package com.ops.sc.server.dao.impl;

import com.ops.sc.common.model.TransMQProducer;
import com.ops.sc.mybatis.mapper.TransMqProducerMapper;
import com.ops.sc.server.dao.TransMQProducerDao;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
public class TransMQProducerDaoImpl implements TransMQProducerDao {

    @Resource
    private TransMqProducerMapper transMqProducerMapper;

    @Override
    public void save(TransMQProducer transMqProducer) {
        transMqProducer.setCreateTime(new Date());
        transMqProducer.setLastUseTime(new Date());
        transMqProducerMapper.save(transMqProducer);
    }

    @Override
    public Long findIdByAllFiled(TransMQProducer transMqProducer) {
        return transMqProducerMapper.findIdByAllFields(transMqProducer);
    }

    @Override
    public void updateLastUseTime(Long id) {
        transMqProducerMapper.updateLastUseTime(id, new Date());
    }

    @Override
    public TransMQProducer findById(Long id) {
        return transMqProducerMapper.findById(id);
    }

}
