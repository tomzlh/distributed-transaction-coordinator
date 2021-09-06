package com.ops.sc.server.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ClientTransMsgQueueRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientTransMsgQueueRegister.class);
    private static Map<String, TransMsgOperation> transactionMessageMap = new ConcurrentHashMap<>();

    private ClientTransMsgQueueRegister() {
    }

    public static ClientTransMsgQueueRegister getInstance() {
        return InstanceBuilder.instance;
    }

    public void registerProducer(String producerName, TransMsgOperation transMsgOperation) {
        if (transactionMessageMap.containsKey(producerName)) {
            throw new IllegalArgumentException("Producer " + producerName + " has already been registered");
        }
        transactionMessageMap.put(producerName, transMsgOperation);
        LOGGER.info("Local producer {} register Success!", producerName);
    }

    public TransMsgOperation getTransactionMessageQueue(String producerName) {
        return transactionMessageMap.get(producerName);
    }

    private static class InstanceBuilder {
        static ClientTransMsgQueueRegister instance = new ClientTransMsgQueueRegister();
    }
}
