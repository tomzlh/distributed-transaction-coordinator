package com.ops.sc.common.enums;


public enum CallbackStrategy {

    IN_ORDER(0, "顺序"), OUT_OF_ORDER(1, "乱序");

    private Integer value;
    private String description;

    CallbackStrategy(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static CallbackStrategy getDefault() {
        return CallbackStrategy.IN_ORDER;
    }

    public static CallbackStrategy getCallbackStrategyByValue(int value) {
        for (CallbackStrategy callbackStrategy : CallbackStrategy.values()) {
            if (callbackStrategy.value == value) {
                return callbackStrategy;
            }
        }

        throw new IllegalArgumentException("Undefined callbackStrategy!");
    }

    public Integer getValue() {
        return value;
    }

}
