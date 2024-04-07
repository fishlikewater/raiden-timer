package com.github.fishlikewater.timer.core;

import com.github.fishlikewater.timer.core.config.TimerConfig;
import com.github.fishlikewater.timer.core.timer.TimerLauncher;
import com.github.fishlikewater.timer.core.utils.CronSequenceGenerator;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2024年04月04日 23:57
 **/
public class ScheduleTaskTest {

    TimerLauncher timerLauncher;

    @Before
    public void init() {
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setTickMs(1000);
        timerConfig.setWheelSize(60);
        timerConfig.setClock(Duration.ofMillis(20));
        timerConfig.setWorkerCoreThreads(1);
        timerConfig.setWorkerMaxThreads(20);
        timerConfig.setWorkerTimeUnit(TimeUnit.MILLISECONDS);
        timerConfig.setWorkerKeepAliveTime(10);
        timerConfig.setWorkerQueueSize(0);
        timerConfig.setWorkerThreadNamePrefix("thread-worker");

        timerLauncher = new TimerLauncher(timerConfig);
    }

    @Test
    public void testCornSchedule() throws InterruptedException {
        final BaseTimerTask task = new BaseTimerTask() {
            @Override
            public void run() {
                System.out.println("这是corn测试");
            }
        };
        task.setCornExpression("0/1 * * * * ?");
        timerLauncher.add(task);
        Thread.sleep(15_000);
    }

    @Test
    public void testAddTask() throws InterruptedException {

        final BaseTimerTask task = new BaseTimerTask() {
            @Override
            public void run() {
                System.out.println("这是具体执行逻辑");
            }
        };
        task.setDelayMs(5_000);
        final BaseTimerTask task2 = new BaseTimerTask() {
            @Override
            public void run() {
                System.out.println("这是具体执行逻辑222");
            }
        };
        task2.setDelayMs(5_000);
        timerLauncher.add(task);
        timerLauncher.add(task2);
        Thread.sleep(15_000);
    }

    @Test
    public void testCorn() {
        final String corn = "30 0/5 * * * ?";
        long next = new CronSequenceGenerator(corn).next(System.currentTimeMillis());
        System.out.println(next);
    }

}
