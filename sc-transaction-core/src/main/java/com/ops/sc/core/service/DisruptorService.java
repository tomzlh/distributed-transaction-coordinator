package com.ops.sc.core.service;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.ops.sc.common.bean.ScRequestMessage;
import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.core.build.RpcMessageBuilder;
import com.ops.sc.core.model.RpcTransMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ThreadFactory;


public class DisruptorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisruptorService.class);


    private Disruptor<RpcTransMessage> disruptor;


    public static DisruptorService getInstance(){
        return InstanceHolder.disruptorService;
    }


    public String start(EventHandler eventHandler){
        int BUFFER_SIZE=1024*1024;
        EventFactory<RpcTransMessage> eventFactory = new EventFactory(){

            @Override
            public Object newInstance() {
                return new RpcTransMessage();
            }
        };

        disruptor = new Disruptor(eventFactory, BUFFER_SIZE, new ThreadFactory(){
            @Override
            public Thread newThread(Runnable r) {
                Thread thread=new Thread(r,"Trader-thread-sc"+ new Random().nextInt(100));
                thread.setDaemon(true);
                return thread;
            }
        }, ProducerType.SINGLE, new YieldingWaitStrategy());

        disruptor.handleEventsWith(eventHandler);

        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());
        // 6.启动
        disruptor.start();

        LOGGER.info("sc agent disruptor service started successfully!");
        return "ok";
    }

    public void submit(ScRequestMessage scRequestMessage) throws ScTransactionException {
        RingBuffer<RpcTransMessage> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try
        {
            RpcTransMessage event = ringBuffer.get(sequence);
            RpcMessageBuilder.toRpcTransMessage(event, scRequestMessage);
        }catch (Exception e){
            LOGGER.error("submit transaction message error!{}", scRequestMessage,e);
            throw new ScTransactionException("submit transaction to executor error!",e);
        }
        finally
        {
            ringBuffer.publish(sequence);
        }
    }



    public static class InstanceHolder{
         private static DisruptorService disruptorService =new DisruptorService();
    }
}
