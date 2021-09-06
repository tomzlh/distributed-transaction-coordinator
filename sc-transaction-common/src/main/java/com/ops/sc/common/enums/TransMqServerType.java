package com.ops.sc.common.enums;


public enum TransMqServerType {

    RABBIT_MQ(0, "RabbitMQ"),

    KAFKA(1, "Kafka");

    private Integer value;

    private String desc;

    TransMqServerType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static TransMqServerType getByValue(Integer value) {
        for (TransMqServerType transMqServerType : TransMqServerType.values()) {
            if (transMqServerType.value.equals(value)) {
                return transMqServerType;
            }
        }
        throw new RuntimeException("Not defined transaction mq Server Type!");

    }

    public String getDesc() {
        return desc;
    }

    public Integer getValue() {
        return value;
    }
}
