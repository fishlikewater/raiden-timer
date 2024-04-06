package com.github.fishlikewater.timer.core.config;

import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2024年04月06日 10:30
 **/

@Data
public class TimerConfig {

    private int tickMs;

    private int wheelSize;

    private int workerMaxThreads;

    private int workerCoreThreads;

    private int workerKeepAliveTime;

    private TimeUnit workerTimeUnit;

    private int workerQueueSize;

    private String workerThreadNamePrefix;

}
