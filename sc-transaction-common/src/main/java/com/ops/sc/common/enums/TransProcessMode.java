package com.ops.sc.common.enums;

import java.util.Arrays;
import java.util.Optional;


public enum TransProcessMode {

    TCC(0),

    MQ_REMOTE(1),

    FMT(2),

    MQ_LOCAL(3),

    LOGIC_BRANCH(4),

    MQ_NATIVE_LOCAL(5),

    MQ_NATIVE_REMOTE(6),

    XA(7),

    SAGA(8);

    private Integer value;

    TransProcessMode(Integer value) {
        this.value = value;
    }

    public static TransProcessMode fromId(int value) {
        for (TransProcessMode type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

    public static TransProcessMode getModeByValue(Integer value) {
        Optional<TransProcessMode> transferMode = Arrays.stream(TransProcessMode.values())
                .filter(mode -> mode.getValue().equals(value)).findAny();
        return transferMode.orElseThrow(IllegalArgumentException::new);
    }

    public static boolean isMQBranch(TransProcessMode mode) {
        return TransProcessMode.MQ_REMOTE == mode || TransProcessMode.MQ_NATIVE_REMOTE == mode
                || TransProcessMode.MQ_LOCAL == mode || TransProcessMode.MQ_NATIVE_LOCAL == mode;
    }

    public static boolean isRemoteMQBranch(TransProcessMode mode) {
        return TransProcessMode.MQ_REMOTE == mode || TransProcessMode.MQ_NATIVE_REMOTE == mode;
    }

    public static boolean isLocalMQBranch(TransProcessMode mode) {
        return TransProcessMode.MQ_LOCAL == mode || TransProcessMode.MQ_NATIVE_LOCAL == mode;
    }

    public Integer getValue() {
        return value;
    }
}