package com.ops.sc.server.service;

import java.time.Duration;

import com.ops.sc.core.config.ProducerConfigMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.ops.sc.common.constant.ServerConstants;
import com.ops.sc.common.exception.ScMessageException;
import com.ops.sc.server.transaction.TransMsgOperation;

@Component
public class TransMessageQueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransMessageQueueService.class);

    private static final Cache<String, TransMsgOperation> TRANS_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMillis(ServerConstants.MQConst.MQ_CONNECTION_EXPIRE))
            .maximumSize(ServerConstants.MQConst.MQ_CONNECTION_MAX)
            .removalListener(TransMessageQueueService::closeMessageProducer).build();

    private static void closeMessageProducer(String key, TransMsgOperation transMsgOperation,
            RemovalCause removalCause) {
        try {
            if (transMsgOperation != null) {
                transMsgOperation.close();
            }
        } catch (ScMessageException e) {
            LOGGER.error("Key : {} connection close error.", key, e);
        }
        LOGGER.debug("Key : {} removed for {}", key, removalCause.name());
    }

    /**
     * get方法在key不存在时自动创建新的value放入缓存 -> 底层调用ConcurrentHashMap的compute方法完成并发控制
     *
     * @param producerName
     * @param type
     * @param producerConfig
     * @return
     */
    public TransMsgOperation getOrCreateTransactionMessageQueue(String producerName, Integer type,
                                                                String producerConfig) {
        return TRANS_CACHE.get(producerName, key -> createTransactionMessageQueue(key, type, producerConfig));
    }

    private TransMsgOperation createTransactionMessageQueue(String key, Integer type, String producerConfig) {
        LOGGER.info("CreateTransactionMessageQueue for {} and type:{},config:{}", key, type, producerConfig);
        return TransMsgOperation.newInstance(type, ProducerConfigMap.fromJsonStr(producerConfig));
    }

}
