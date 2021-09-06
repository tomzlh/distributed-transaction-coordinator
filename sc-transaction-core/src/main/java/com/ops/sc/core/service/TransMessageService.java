package com.ops.sc.core.service;

import com.ops.sc.common.model.TransMessage;
import com.ops.sc.common.model.TransMQProducer;

/**
 * 投递事务消息服务类
 *
 */
public interface TransMessageService {

    TransMQProducer getProducerByProducerId(String producerId);

    TransMessage getByBranchId(Long bid);

    void delete(Long tid);

}
