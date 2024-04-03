package com.github.fishlikewater.timer.core.timer;

import com.github.fishlikewater.timer.core.Bucket;
import com.github.fishlikewater.timer.core.TimeWheel;
import com.github.fishlikewater.timer.core.TimerTask;
import com.github.fishlikewater.timer.core.TimerTaskEntry;
import com.github.fishlikewater.timer.core.utils.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * {@code TimerLauncher}
 * 定时器实现
 *
 * @author zhangxiang
 * @date 2024/04/03
 * @since 1.0.0
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


    public TimerLauncher() {
        // TODO 带完善
        this.timeWheel = new TimeWheel(1, 20, System.currentTimeMillis(), delayQueue);
        this.workerThreadPool = new ThreadPoolExecutor(
                20,
                20,
                1,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory("timer-worker"));
        this.bossThreadPool = new ThreadPoolExecutor(
                1,
                1,
                1,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory("timer-boss"));

        // 20ms推动一次时间轮运转
        this.bossThreadPool.submit(() -> {
            while (true) {
                this.advanceClock(20);
            }
        });
    }


    public void addTimerTaskEntry(TimerTaskEntry entry) {
        if (!timeWheel.addTask(entry)) {
            // 任务已到期
            TimerTask timerTask = entry.getTimerTask();
            log.info("=====任务:{} 已到期,准备执行============", timerTask.getDesc());
            workerThreadPool.submit(timerTask);
        }
    }

    @Override
    public void add(TimerTask timerTask) {
        log.info("=======添加任务开始====task:{}", timerTask.getDesc());
        TimerTaskEntry entry = new TimerTaskEntry(timerTask, timerTask.getDelayMs() + System.currentTimeMillis());
        timerTask.setTimerTaskEntry(entry);
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
