package com.ops.sc.server.service.impl;

import com.ops.sc.common.model.TransMQProducer;
import com.ops.sc.rpc.dto.MQProducerRegRequest;
import com.ops.sc.server.service.MetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ops.sc.server.dao.TransMQProducerDao;


@Service
public class MetaServiceImpl implements MetaService {

    @Autowired
    private TransMQProducerDao transMqProducerDao;

    /**
     * 获取producerId
     *
     * @param request
     * @return
     */
    @Override
    public Long getMqProducerId(final MQProducerRegRequest request) {
        TransMQProducer transMqProducer = new TransMQProducer(request.getType().getValue(), request.getConfig());
        Long producerId = transMqProducerDao.findIdByAllFiled(transMqProducer);
        if (producerId == null) {
            transMqProducerDao.save(transMqProducer);
            producerId = transMqProducer.getId();
        } else {
            transMqProducerDao.updateLastUseTime(producerId);
        }
        return producerId;
    }

}
