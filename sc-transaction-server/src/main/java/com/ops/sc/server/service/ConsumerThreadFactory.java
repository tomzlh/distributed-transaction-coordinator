package com.ops.sc.server.service;

import java.util.concurrent.ThreadFactory;

public class ConsumerThreadFactory implements ThreadFactory {

    public String workerName;
    @Override
    public Thread newThread(Runnable r) {
        Thread thread=new Thread(r,"Trader-thread"+workerName);
        thread.setDaemon(true);
        return thread;
    }
}
