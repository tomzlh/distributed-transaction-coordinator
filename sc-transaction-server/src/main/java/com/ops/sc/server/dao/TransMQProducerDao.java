package com.ops.sc.server.dao;

import com.ops.sc.common.model.TransMQProducer;


public interface TransMQProducerDao {

    void save(TransMQProducer transMqProducer);

    TransMQProducer findById(Long id);

    Long findIdByAllFiled(TransMQProducer transMqProducer);

    void updateLastUseTime(Long id);
}
