package com.ops.sc.common.enums;

import java.util.Arrays;
import java.util.Optional;

public enum TransactionType {

    TRANSACTION(0),

    ROLLBACK(1);

    private Integer value;

    TransactionType(Integer value) {
        this.value = value;
    }

    public static TransactionType fromId(int value) {
        for (TransactionType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

    public static TransactionType getModeByValue(Integer value) {
        Optional<TransactionType> transactionType = Arrays.stream(TransactionType.values())
                .filter(mode -> mode.getValue().equals(value)).findAny();
        return transactionType.orElseThrow(IllegalArgumentException::new);
    }

    public Integer getValue() {
        return value;
    }

}
