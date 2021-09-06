package com.ops.sc.common.model;

import lombok.Data;

import java.util.Date;


@Data
public class TransMQProducer {

    private Long id;

    private Integer type; // mq类型，0:RabbitMQ，1:kafka

    private String config;

    private Date lastUseTime;

    private Date createTime;

    public TransMQProducer() {
    }

    public TransMQProducer(Integer type, String config) {
        this.type = type;
        this.config = config;
    }

}
