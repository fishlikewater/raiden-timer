package com.github.fishlikewater.timer.core.timer;

import com.github.fishlikewater.timer.core.BaseTimerTask;

/**
 * {@code Timer}
 * 定时器接口
 *
 * @author zhangxiang
 * @date 2024/04/03
 * @since 1.0.0
 */
public interface Timer {
    /**
     * 添加一个新任务
     *
     * @param baseTimerTask {@link BaseTimerTask}
     */
    void add(BaseTimerTask baseTimerTask);

    /**
     * 推动指针
     *
     * @param timeout 超时时间
     */
    void advanceClock(long timeout);

    /**
     * 等待执行的任务
     *
     * @return {@link Number}
     */
    int size();

    /**
     * 关闭服务,剩下的无法被执行
     */
    void shutdown();
}
