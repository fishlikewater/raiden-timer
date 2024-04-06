package com.github.fishlikewater.timer.core;

import com.github.fishlikewater.timer.core.config.TimerConfig;
import com.github.fishlikewater.timer.core.timer.TimerLauncher;
import org.junit.Test;

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

    @Test
    public void testAddTask() throws InterruptedException {
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setTickMs(1);
        timerConfig.setWheelSize(20);
        timerConfig.setWorkerCoreThreads(1);
        timerConfig.setWorkerMaxThreads(20);
        timerConfig.setWorkerTimeUnit(TimeUnit.MILLISECONDS);
        timerConfig.setWorkerKeepAliveTime(10);
        timerConfig.setWorkerQueueSize(0);
        timerConfig.setWorkerThreadNamePrefix("thread-worker");

        final TimerLauncher timerLauncher = new TimerLauncher(timerConfig);
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("这是具体执行逻辑");
            }
        };
        task.setDelayMs(5_000);
        final TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                System.out.println("这是具体执行逻辑222");
            }
        };
        task2.setDelayMs(5_000);
        timerLauncher.add(task);
        timerLauncher.add(task2);
        Thread.sleep(10_000);
    }

}
