package com.ops.sc.server.service.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.ops.sc.common.model.TransactionInfo;
import com.ops.sc.server.handler.GrpcServerTransEventHandler;
import com.ops.sc.server.service.GlobalTransService;
import com.ops.sc.server.service.TransExecuteService;
import com.ops.sc.server.service.ConsumerThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


@Service
public class TransExecuteServiceImpl implements TransExecuteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransExecuteServiceImpl.class);

    @Resource
    private GlobalTransService globalTransService;


    Disruptor<TransactionInfo> disruptor;


    @PostConstruct
    public String start(){

        int BUFFER_SIZE=1024*1024;

        EventFactory<TransactionInfo> eventFactory = new EventFactory(){

            @Override
            public Object newInstance() {
                return new TransactionInfo();
            }
        };

        ConsumerThreadFactory consumerThreadFactory =new ConsumerThreadFactory();

        consumerThreadFactory.workerName="dsp";

        disruptor = new Disruptor(eventFactory, BUFFER_SIZE, consumerThreadFactory, ProducerType.SINGLE, new YieldingWaitStrategy());
        // 5.连接消费端方法
        disruptor.handleEventsWith(new GrpcServerTransEventHandler());

        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());
        // 6.启动
        disruptor.start();

        LOGGER.info("disruptor service started successfully!");

        return "ok";
    }

    @Override
    public void submitGlobalTrans(TransactionInfo transactionInfo) {
        //TransactionInfo transactionInfo = globalTransService.getByTid(transReportEvent.getTid());
        RingBuffer<TransactionInfo> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try
        {
            TransactionInfo event = ringBuffer.get(sequence);
            BeanUtils.copyProperties(transactionInfo,event);
        }
        finally
        {
            ringBuffer.publish(sequence);
        }
    }


}
