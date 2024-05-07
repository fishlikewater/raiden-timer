/*
 * Copyright © 2024 zhangxiang (fishlikewater@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fishlikewater.fishlikewater.raiden.timer.core.timer;

import com.fishlikewater.fishlikewater.raiden.timer.core.BaseTimerTask;
import com.fishlikewater.fishlikewater.raiden.timer.core.Bucket;
import com.fishlikewater.fishlikewater.raiden.timer.core.TimeWheel;
import com.fishlikewater.fishlikewater.raiden.timer.core.TimerTaskEntry;
import com.fishlikewater.fishlikewater.raiden.timer.core.config.TimerConfig;
import com.fishlikewater.fishlikewater.raiden.timer.core.utils.CronSequenceGenerator;
import com.fishlikewater.fishlikewater.raiden.timer.core.utils.NamedThreadFactory;
import com.fishlikewater.fishlikewater.raiden.timer.core.utils.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * {@code TimerLauncher}
 * 定时器实现
 *
 * @author zhangxiang
 * @since  2024/04/03
 * @version 1.0.0
 */
@Slf4j
public class TimerLauncher implements Timer {

    /**
     * 底层时间轮
     */
    private TimeWheel timeWheel;
    /**
     * 一个Timer只有一个延时队列
     */
    private final DelayQueue<Bucket> delayQueue = new DelayQueue<>();
    /**
     * 过期任务执行线程
     */
    private final ExecutorService workerThreadPool;
    /**
     * 轮询delayQueue获取过期任务线程
     */
    private final ExecutorService bossThreadPool;

    @Getter
    private final TimerConfig timerConfig;

    public TimerLauncher(TimerConfig timerConfig) {
        this.timerConfig = timerConfig;
        this.timeWheel = new TimeWheel(timerConfig.getTickMs().toSeconds(), timerConfig.getWheelSize(), System.currentTimeMillis(), delayQueue);
        this.workerThreadPool = Executors.newVirtualThreadPerTaskExecutor();
        this.bossThreadPool = new ThreadPoolExecutor(
                1,
                1,
                0,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory("timer-boss"));

        // 20ms推动一次时间轮运转
        this.bossThreadPool.submit(() -> {
            while (true) {
                this.advanceClock(timerConfig.getClock().toMillis());
            }
        });
    }

    public void addTimerTaskEntry(TimerTaskEntry entry) {
        if (!timeWheel.addTask(entry)) {
            // 任务已到期
            BaseTimerTask baseTimerTask = entry.getBaseTimerTask();
            log.info("执行任务: {}", baseTimerTask.getDesc());
            workerThreadPool.submit(baseTimerTask);
            // corn 表达式任务添加下次时间
            if (Objects.nonNull(entry.getCronSequenceGenerator())) {
                entry.setExpireMs(entry.getCronSequenceGenerator().next(System.currentTimeMillis()));
                this.addTimerTaskEntry(entry);
            }
        }
    }

    @Override
    public void add(BaseTimerTask baseTimerTask) {
        log.info("添加任务:{}", baseTimerTask.getDesc());
        TimerTaskEntry entry;
        if (StringUtils.hasLength(baseTimerTask.getCornExpression())) {
            CronSequenceGenerator generator = new CronSequenceGenerator(baseTimerTask.getCornExpression());
            long expireMs = generator.next(System.currentTimeMillis());
            entry = new TimerTaskEntry(baseTimerTask, expireMs);
            entry.setCronSequenceGenerator(generator);
        } else {
            entry = new TimerTaskEntry(baseTimerTask, baseTimerTask.getDelayMs() + System.currentTimeMillis());
        }
        baseTimerTask.setTimerTaskEntry(entry);
        addTimerTaskEntry(entry);
    }

    /**
     * 推动指针运转获取过期任务
     *
     * @param timeout 时间间隔
     */
    @Override
    public synchronized void advanceClock(long timeout) {
        try {
            Bucket bucket = delayQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (bucket != null) {
                // 推进时间
                timeWheel.advanceLock(bucket.getExpiration());
                // 执行过期任务(包含降级)
                bucket.clear(this::addTimerTaskEntry);
            }
        } catch (InterruptedException e) {
            log.error("advanceClock error");
        }
    }

    @Override
    public int size() {
        return delayQueue.size();
    }

    @Override
    public void shutdown() {
        this.bossThreadPool.shutdown();
        this.workerThreadPool.shutdown();
        this.timeWheel = null;
    }
}
