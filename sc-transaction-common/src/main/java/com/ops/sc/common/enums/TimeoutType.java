package com.ops.sc.common.enums;


public enum TimeoutType {

    ALARM(0, "超时报警策略"), CANCEL(1, "超时回滚策略");

    private Integer value;
    private String description;

    TimeoutType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static TimeoutType getDefault() {
        return TimeoutType.ALARM;
    }

    public static TimeoutType getByValue(int value) {
        for (TimeoutType timeoutType : TimeoutType.values()) {
            if (timeoutType.value == value) {
                return timeoutType;
            }
        }

        throw new IllegalArgumentException("Undefined timeoutStrategy!");
    }

    public Integer getValue() {
        return value;
    }

}
