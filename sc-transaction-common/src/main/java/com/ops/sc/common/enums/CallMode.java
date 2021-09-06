package com.ops.sc.common.enums;

public enum CallMode {

    SYNC(1),
    PARALLEL(2);

    private int value;

    CallMode(Integer value) {
        this.value = value;
    }

    public static CallMode getCallModeByValue(Integer value) {
        for (CallMode callMode : CallMode.values()) {
            if (callMode.value==value) {
                return callMode;
            }
        }
        throw new IllegalArgumentException("Undefined TransStatus!");
    }

    public int getValue(){
        return value;
    }
}
