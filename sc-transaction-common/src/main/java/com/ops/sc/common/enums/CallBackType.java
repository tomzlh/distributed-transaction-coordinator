package com.ops.sc.common.enums;


public enum CallBackType {

    COMMIT(0),

    ROLLBACK(1),

    CHECKBACK(3),

    LOCAL_COMPENSATE(4),

    RETRY(5),

    CANCEL_TIMEOUT_BRANCH(6),

    DELETE_LOG(7);

    private int value;

    CallBackType(int value) {
        this.value = value;
    }

    public static CallBackType fromId(int value) {
        for (CallBackType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }


    public int getValue() {
        return value;
    }

}
