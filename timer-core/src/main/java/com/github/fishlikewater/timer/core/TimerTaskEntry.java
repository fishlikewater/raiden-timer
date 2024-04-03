package com.github.fishlikewater.timer.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code TimerTaskEntry}
 * 定时任务
 *
 * @author zhangxiang
 * @date 2024/04/03
 * @since 1.0.0
 */
@Data
@Slf4j
public class TimerTaskEntry implements Comparable<TimerTaskEntry> {

    volatile Bucket bucket;
    TimerTaskEntry next;
    TimerTaskEntry prev;
    private TimerTask timerTask;
    private long expireMs;

    public TimerTaskEntry(TimerTask timedTask, long expireMs) {
        this.timerTask = timedTask;
        this.expireMs = expireMs;
        this.next = null;
        this.prev = null;
    }

    void remove() {
        Bucket currentList = bucket;
        while (currentList != null) {
            currentList.remove(this);
            currentList = bucket;
        }
    }

    @Override
    public int compareTo(TimerTaskEntry o) {
        return ((int) (this.expireMs - o.expireMs));
    }
}