package com.ops.sc.common.enums;

import java.util.Arrays;
import java.util.Optional;



public enum TransIsolation {
    READ_UNCOMMITTED("RU"), READ_COMMITTED("RC");

    private String value;

    TransIsolation(String value) {
        this.value = value;
    }

    public static TransIsolation getIsolationByValue(String value) {
        Optional<TransIsolation> isolationLevel = Arrays.stream(TransIsolation.values())
                .filter(level -> level.value.equals(value)).findAny();
        return isolationLevel.orElse(null);
    }

    public String getValue() {
        return value;
    }
}
