package com.ops.sc.server.service.impl;

import javax.annotation.Resource;
import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ops.sc.common.model.TransMessage;
import com.ops.sc.common.model.TransMQProducer;
import com.ops.sc.core.service.TransMessageService;
import com.ops.sc.server.dao.TransMQProducerDao;
import com.ops.sc.server.dao.TransMsgDao;
import org.springframework.stereotype.Service;


@Service
public class TransMessageServiceImpl implements TransMessageService {

    // 远程事务消息对应的MQ个数
    private static final Integer MQ_PRODUCER_CACHE_MAX_NUM = 100;

    // producerId是数据库主键ID，不会重复，因此缓存时间可以增加
    private static final Integer MQ_PRODUCER_EXPIRE = 12 * 30 * 60 * 1000;

    // expireAfterAccess:设置Key多长时间没有被访问就驱逐
    private static final Cache<String, TransMQProducer> PRODUCER_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMillis(MQ_PRODUCER_EXPIRE)).maximumSize(MQ_PRODUCER_CACHE_MAX_NUM).build();

    @Resource
    private TransMQProducerDao transMQProducerDao;

    @Resource
    private TransMsgDao transMsgDao;

    @Override
    public void delete(Long tid) {
        transMsgDao.delete(tid);
    }

    /**
     * get方法在key不存在时自动创建新的value放入缓存 -> 底层调用ConcurrentHashMap的compute方法完成并发控制
     *
     * @param producerId
     * @return
     */
    @Override
    public TransMQProducer getProducerByProducerId(String producerId) {
        return PRODUCER_CACHE.get(producerId, key -> transMQProducerDao.findById(Long.valueOf(key)));
    }

    @Override
    public TransMessage getByBranchId(Long branchId) {
        return transMsgDao.findByBranchId(branchId);
    }

}
