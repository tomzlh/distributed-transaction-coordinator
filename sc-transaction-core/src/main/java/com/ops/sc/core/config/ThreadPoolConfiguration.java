package com.ops.sc.core.config;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.springframework.stereotype.Component;

/**
 * 线程池配置
 *
 */
@Configuration
@ConfigurationProperties(prefix = "traceTask.pool")
@EnableAsync
public class ThreadPoolConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolConfiguration.class);


    @Value("${traceTask.pool.corePoolSize}")
    private Integer traceTaskCorePoolSize;

    @Value("${traceTask.pool.maxPoolSize}")
    private Integer traceTaskMaxPoolSize;

    @Value("${traceTask.pool.keepAliveSeconds}")
    private Integer traceTaskKeepAliveSeconds;

    @Value("${traceTask.pool.queueCapacity}")
    private Integer traceTaskQueueCapacity;



    @Bean("commonTask")
    public Executor traceTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(traceTaskCorePoolSize);
        executor.setMaxPoolSize(traceTaskMaxPoolSize);
        executor.setQueueCapacity(traceTaskQueueCapacity);
        executor.setKeepAliveSeconds(traceTaskKeepAliveSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("traceTask-");
        executor.setRejectedExecutionHandler(
                (runnable, poolExecutor) -> LOGGER.info("Reject the task, task info : {}", runnable.toString()));
        executor.initialize();
        return executor;
    }


}
