package com.ops.sc.common.enums;


public enum LocalInvokeType {

    COMMIT(0),

    ROLLBACK(1);

    private int value;

    LocalInvokeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
