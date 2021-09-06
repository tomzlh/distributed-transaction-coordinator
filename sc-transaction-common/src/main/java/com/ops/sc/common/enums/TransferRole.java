package com.ops.sc.common.enums;


public enum TransferRole {
    STARTER(1),

    PARTICIPATOR(2);

    private Integer value;

    TransferRole(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
