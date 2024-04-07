package com.github.fishlikewater.timer.core;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code BaseTimerTask}
 * 执行任务
 *
 * @author zhangxiang
 * @date 2024/04/03
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseTimerTask implements Runnable {

    /**
     * 延时时间
     */
    private long delayMs;

    /**
     * corn表达式
     */
    private String cornExpression;

    /**
     * 任务所在的entry
     */
    private TimerTaskEntry timerTaskEntry;

    private String desc;

    public synchronized void setTimerTaskEntry(TimerTaskEntry entry) {
        if (timerTaskEntry != null && timerTaskEntry != entry) {
            timerTaskEntry.remove();
        }
        timerTaskEntry = entry;
    }


}
