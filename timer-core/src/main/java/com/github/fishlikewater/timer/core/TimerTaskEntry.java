package com.github.fishlikewater.timer.core;

import com.github.fishlikewater.timer.core.utils.CronSequenceGenerator;
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
    private BaseTimerTask baseTimerTask;
    private CronSequenceGenerator cronSequenceGenerator;
    private long expireMs;

    public TimerTaskEntry() {}

    public TimerTaskEntry(BaseTimerTask baseTimerTask, long expireMs) {
        this.baseTimerTask = baseTimerTask;
        this.expireMs = expireMs;
        this.next = null;
        this.prev = null;
    }

    void remove() {
        Bucket currentBucket = bucket;
        while (currentBucket != null) {
            currentBucket.remove(this);
            currentBucket = bucket;
        }
    }

    @Override
    public int compareTo(TimerTaskEntry o) {
        return ((int) (this.expireMs - o.expireMs));
    }
}