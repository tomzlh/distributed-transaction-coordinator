package com.ops.sc.common.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageInfo {
    @JSONField(name = "ProducerName")
    private String producerName;

    @JSONField(name = "MetaData")
    private String metaData;

    @JSONField(name = "Payload")
    private String payload;

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
