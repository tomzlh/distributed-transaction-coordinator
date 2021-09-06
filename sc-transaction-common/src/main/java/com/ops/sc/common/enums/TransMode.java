package com.ops.sc.common.enums;


public enum TransMode {

    TCC(0),

    FMT(1),

    XA(2),

    SAGA(3);

    private int value;

    TransMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TransMode fromId(int value) {
        for (TransMode transMode : values()) {
            if (transMode.getValue() == value) {
                return transMode;
            }
        }
        return null;
    }

}
