package com.ops.sc.server.service;


import com.ops.sc.rpc.dto.MQProducerRegRequest;

public interface MetaService {

    /**
     * 获取producerId
     *
     * @param request
     * @return
     */
    Long getMqProducerId(final MQProducerRegRequest request);

}
