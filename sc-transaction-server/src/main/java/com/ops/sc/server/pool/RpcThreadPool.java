package com.ops.sc.server.pool;

import com.ops.sc.common.exception.RequestException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@ConfigurationProperties(prefix = "rpcServer.pool")
public class RpcThreadPool {

    @Getter
    @Setter
    private Integer corePoolSize;
    @Getter
    @Setter
    private Integer maxPoolSize;
    @Getter
    @Setter
    private Integer keepAliveSeconds;
    @Getter
    @Setter
    private Integer queueCapacity;

    @Bean("rpcServerTask")
    public Executor rpcServerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("rpcServerTask-");
        executor.setRejectedExecutionHandler((runnable, poolExecutor) -> {
            throw new RequestException(runnable.toString());
        });
        executor.initialize();
        return executor;
    }
}
