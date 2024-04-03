package com.github.fishlikewater.timer.core;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code TimerTask}
 * 执行任务
 *
 * @author zhangxiang
 * @date 2024/04/03
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TimerTask implements Runnable, Serializable {

    @Serial
    private static final long serialVersionUID = -8435128999145996179L;
    /**
     * 延时时间
     */
    private long delayMs;
    /**
     * 任务所在的entry
     */
    private TimerTaskEntry timerTaskEntry;

    private String desc;

    public TimerTask(String desc, long delayMs) {
        this.desc = desc;
        this.delayMs = delayMs;
        this.timerTaskEntry = null;
    }

    public synchronized void setTimerTaskEntry(TimerTaskEntry entry) {
        if (timerTaskEntry != null && timerTaskEntry != entry) {
            timerTaskEntry.remove();
        }
        timerTaskEntry = entry;
    }

    @Override
    public void run() {

    }
}
