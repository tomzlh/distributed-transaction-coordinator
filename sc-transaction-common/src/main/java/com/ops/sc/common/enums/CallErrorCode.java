package com.ops.sc.common.enums;

import java.util.Arrays;
import java.util.Optional;



public enum CallErrorCode {

    SUCCESS(0, "Success"),

    TIMEOUT_EXCEPTION(1, "Timeout"),

    CALLBACK_EXECUTE_FAILED(2, "ExecuteFailed"),

    NO_AVAILABLE_CONNECTION(3, "NoAvailableConnection"),

    NO_DATASOURCE(4, "NoDataSource"), // 无法找到对应的datasource

    SQL_EXCEPTION(5, "SqlException"),

    REFLECT_FAILED(6, "ReflectFailed"), // 反射失败

    INVOCATION_TARGET_EXCEPTION(7, "InvocationTargetException"),

    IMAGE_NOT_CONSISTENT(8, "ImageNotConsistent"),

    UNKNOWN_EXCEPTION(9, "UnknownException"),

    RPC_EXCEPTION(10, "RPCException"),

    UnknownAppError(11, "Unknown error"),

    NetConnect(12, "Can not connect to the server"),

    RegisterError(13, "Register Service failed"),

    NoAvailableService(14, "No available service"),

    NetDispatch(15, "Dispatch error"),

    ThreadPoolFull(16, "Thread pool is full");

    private static final String RESOURCE_KEY_PREFIX = "callBackError";
    private Integer value;
    private String errorCode;

    CallErrorCode(Integer value, String errorCode) {
        this.value = value;
        this.errorCode = errorCode;
    }

    public static CallErrorCode getCallBackErrorByCodeNum(Integer value) throws IllegalArgumentException {
        Optional<CallErrorCode> callBackErrorInfoType = Arrays.stream(CallErrorCode.values())
                .filter(type -> type.getValue().equals(value)).findAny();
        return callBackErrorInfoType.orElseThrow(IllegalArgumentException::new);
    }

    public Integer getValue() {
        return value;
    }

    public String getResourceKey() {
        return RESOURCE_KEY_PREFIX + "." + errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isSuccess() {
        return SUCCESS.value.equals(this.value);
    }
}
