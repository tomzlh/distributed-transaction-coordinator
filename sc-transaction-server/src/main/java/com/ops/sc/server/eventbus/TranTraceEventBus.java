package com.ops.sc.server.eventbus;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;
import com.ops.sc.server.event.BranchTransEvent;
import com.ops.sc.server.event.TransEvent;
import com.ops.sc.server.listener.EventListener;
import com.ops.sc.server.listener.impl.TransEventListener;
import com.ops.sc.server.service.BranchTransService;
import com.ops.sc.server.service.GlobalTransService;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service("tranTraceEventBus")
public class TranTraceEventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranTraceEventBus.class);

    private ExecutorService executorService;

    private EventBus eventBus;

    @Resource
    private BranchTransService branchTransService;

    @Resource
    private GlobalTransService globalTransService;

    @PostConstruct
    public void initTraceEventBus() {
        executorService = createExecutorService(Runtime.getRuntime().availableProcessors() * 2);
        eventBus = new AsyncEventBus(executorService);
        register(new TransEventListener(globalTransService,branchTransService));
    }

    private ExecutorService createExecutorService(final int threadSize) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadSize, threadSize, 5L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(), new BasicThreadFactory.Builder().namingPattern(String.join("-", "trans-event", "%s")).build());
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return MoreExecutors.listeningDecorator(MoreExecutors.getExitingExecutorService(threadPoolExecutor));
    }

    public void register(final EventListener eventListener) {
        try {
            eventBus.register(eventListener);
        } catch (Exception ex) {
            LOGGER.error("create Event listener failed, error: ", ex);
        }
    }


    public void post(final TransEvent event) {
        if (!executorService.isShutdown()) {
            eventBus.post(event);
        }
    }
}
