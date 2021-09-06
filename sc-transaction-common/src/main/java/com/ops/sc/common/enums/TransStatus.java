package com.ops.sc.common.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public enum TransStatus {

    TRYING(0, "处于try阶段中"),

    TRY_SUCCEED(1, "try成功"),

    TRY_FAILED(2, "try失败"),

    COMMITTING(3, "处于confirm阶段中"),

    COMMIT_SUCCEED(4, "confirm成功"),

    COMMIT_FAILED(5, "confirm失败"),

    CANCELLING(6, "处于cancel阶段中"),

    CANCEL_SUCCEED(7, "cancel成功"),

    CANCEL_FAILED(8, "cancel失败"),

    TRY_TIMEOUT(9, "Try阶段执行超时"),

    NOT_EXIST(10, "事务不存在"),

    READY(11,"事务准备"),

    EXECUTE_SUCCEED(12,"执行成功"),

    EXECUTE_FAILED(13,"执行失败"),

    RETRY(14,"重试");


    private Integer value;
    private String description;

    TransStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static TransStatus getTransStatusByValue(Integer value) {
        for (TransStatus trans : TransStatus.values()) {
            if (trans.value.equals(value)) {
                return trans;
            }
        }
        throw new IllegalArgumentException("Undefined TransStatus!");
    }



    public boolean isFinalStatus() {
        return this == COMMIT_FAILED || this == COMMIT_SUCCEED || this == CANCEL_FAILED || this == CANCEL_SUCCEED;
    }

    public Integer getValue() {
        return value;
    }

}