package com.ops.sc.common.enums;


public enum ExecutionResult {
    SUCCEED(0, "execution success"),

    FAILED(1, "execution failed"),

    // 未执行表示根据状态有其他线程在执行这个分支事务
    NOT_EXECUTED(2, "not execute");

    private Integer value;
    private String description;

    ExecutionResult(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public boolean isSuccess() {
        return value.equals(0);
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
