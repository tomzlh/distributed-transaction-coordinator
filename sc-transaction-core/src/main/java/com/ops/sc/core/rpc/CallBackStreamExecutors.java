package com.ops.sc.core.rpc;


import com.ops.sc.core.util.ApplicationUtils;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;



public class CallBackStreamExecutors {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallBackStreamExecutors.class);

    private static final String GRPC_CALLBACK_THREAD_POOL_COUNT = "grpc.callback.thread.pool.count";
    private static final int QUEUE_CAPACITY = 10000;
    private static final CallBackStreamExecutors INSTANCE = new CallBackStreamExecutors();

    private List<ThreadPoolTaskExecutor> executors;

    private CallBackStreamExecutors() {
        Environment environment = ApplicationUtils.getBean(Environment.class);
        Integer threadPoolCount = environment.getProperty(GRPC_CALLBACK_THREAD_POOL_COUNT, Integer.class);

        executors = new ArrayList<>(threadPoolCount);
        for (int i = 0; i < threadPoolCount; i++) {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(1);
            executor.setMaxPoolSize(1);
            // 超时时间30s，每个请求平均5ms，设置10000，10000之后的没有必要执行
            executor.setQueueCapacity(QUEUE_CAPACITY);
            executor.setWaitForTasksToCompleteOnShutdown(true);
            // 当线程池满时，不做任何操作，请求业务线程sleep30s，缓解压力
            executor.setRejectedExecutionHandler((runnable, poolExecutor) -> {
                LOGGER.warn("call back thread reject more task");
            });
            executor.initialize();
            executors.add(executor);
        }
    }

    public static CallBackStreamExecutors getInstance() {
        return INSTANCE;
    }

    public void callback(StreamObserver sb, RpcCallBackResponse response) {
        CallBackTask task = new CallBackTask(sb, response);
        executors.get(sb.hashCode() % executors.size()).submit(task);
    }

    private class CallBackTask implements Runnable {
        private StreamObserver<RpcCallBackResponse> streamObserver;
        private RpcCallBackResponse rpcCallBackResponse;

        public CallBackTask(StreamObserver<RpcCallBackResponse> streamObserver,
                RpcCallBackResponse rpcCallBackResponse) {
            this.streamObserver = streamObserver;
            this.rpcCallBackResponse = rpcCallBackResponse;
        }

        @Override
        public void run() {
            try {
                streamObserver.onNext(rpcCallBackResponse);
            } catch (Exception e) {
                LOGGER.warn("call back to client fail: {}", e.getMessage());
            }
        }
    }
}
