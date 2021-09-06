package com.ops.sc.mybatis.mapper;

import com.ops.sc.common.model.TransMQProducer;
import org.apache.ibatis.annotations.Param;

import java.util.Date;


public interface TransMqProducerMapper {
    void save(TransMQProducer transMqProducer);

    TransMQProducer findById(@Param("id") Long id);

    Long findIdByAllFields(TransMQProducer transMqProducer);

    void updateLastUseTime(@Param("id") Long id, @Param("lastUseTime") Date lastUseTime);
}
