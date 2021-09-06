package com.ops.sc.common.enums;

public enum MessageType {

    TYPE_REG(0),

    TYPE_BRANCH_PREPARE(1),

    TYPE_BRANCH_COMMIT(2),

    TYPE_BRANCH_ROLLBACK(3),

    TYPE_HEARTBEAT_MSG(4),

    TYPE_BRANCH_PREPARE_RESP(5),

    TYPE_BRANCH_COMMIT_RESP(6),

    TYPE_BRANCH_ROLLBACK_RESP(7),

    TYPE_BRANCH_STATUS_NOTIFY(8),

    TYPE_TA_REG(9),

    TYPE_HEARTBEAT_REQUEST(10),

    TYPE_HEARTBEAT_RESPONSE(11),

    TYPE_MESSAGE_REQUEST(12),

    TYPE_MESSAGE_RESPONSE(13);


    private int value;

    MessageType(int value) {
        this.value = value;
    }

    public static MessageType getByValue(int value) {
        for (MessageType type : values()) {
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
