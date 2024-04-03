package com.github.fishlikewater.timer.core;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * {@code Bucket}
 * 任务桶
 *
 * @author zhangxiang
 * @date 2024/04/03
 * @since 1.0.0
 */
public class Bucket implements Serializable, Delayed {

    @Serial
    private static final long serialVersionUID = -54685076223584564L;
    /**
     * 虚拟根节点root
     */
    private final TimerTaskEntry root = new TimerTaskEntry(null, -1);
    /**
     * bucket的过期时间
     */
    private final AtomicLong expiration = new AtomicLong(-1L);

    {
        root.next = root;
        root.prev = root;
    }

    public long getExpiration() {
        return expiration.get();
    }

    /**
     * 设置bucket的过期时间,设置成功返回true
     *
     * @param expirationMs 到期时间
     * @return boolean {@link Boolean}
     */
    boolean setExpiration(long expirationMs) {
        return expiration.getAndSet(expirationMs) != expirationMs;
    }

    /**
     * 添加任务
     *
     * @param entry 定时任务
     * @return boolean {@link Boolean}
     */
    public boolean addTask(TimerTaskEntry entry) {
        boolean done = false;
        while (!done) {
            entry.remove();
            synchronized (this) {
                if (entry.bucket == null) {
                    entry.bucket = this;
                    TimerTaskEntry tail = root.prev;
                    entry.prev = tail;
                    entry.next = root;
                    tail.next = entry;
                    root.prev = entry;
                    done = true;
                }
            }
        }
        return true;
    }

    /**
     * 从 Bucket 移除指定的 timerTaskEntry
     *
     * @param entry 定时任务
     */
    public void remove(TimerTaskEntry entry) {
        synchronized (this) {
            if (entry.getBucket().equals(this)) {
                entry.next.prev = entry.prev;
                entry.prev.next = entry.next;
                entry.next = null;
                entry.prev = null;
                entry.bucket = null;
            }
        }
    }

    /**
     * 移除所有
     */
    public synchronized void clear(Consumer<TimerTaskEntry> entry) {
        TimerTaskEntry head = root.next;
        while (!head.equals(root)) {
            remove(head);
            entry.accept(head);
            head = root.next;
        }
        expiration.set(-1L);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return Math.max(0, unit.convert(expiration.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof Bucket bucket) {
            return Long.compare(expiration.get(), bucket.expiration.get());
        }
        return 0;
    }
}
