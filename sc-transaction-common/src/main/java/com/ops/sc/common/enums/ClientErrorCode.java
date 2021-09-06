package com.ops.sc.common.enums;



public enum ClientErrorCode {

    SUCCESS("200"),

    CLIENT_REQUEST_FAILED("10001"),

    LOCAL_DATABASE_FAILED("1002"),

    CLIENT_DATASOURCE_CONFIG_ERROR("10003"),

    UNSUPPORTED_DATABASE("10004"),

    INTERNAL_ERROR("10005"),

    UNSUPPORTED("10006"),

    XA_PREPARE_FAILED("10007"),

    RATE_LIMITER("10008"),

    SERVER_NOT_AVAILABLE("10009"),

    HEART_BEAT_TIMEOUT("10010"),

    LOCK_CONFLICT("10011"),

    UNSUPPORTED_SQL("10012"),

    MSG_CONFIG_ERROR("10013"),

    MSG_SEND_FAILED("10014"),

    MSG_COMMIT_FAILED("10015"),

    MODEL_GET_ERROR("10016"),

    START_GLOBAL_TRANS_ERROR("10017"),

    ROLLBACK_GLOBAL_TRANS_ERROR("10018"),

    COMMIT_TRANS_ERROR("10019"),

    GLOBAL_TRANS_NOT_FOUND("10020"),

    GLOBAL_TRANS_QUERY_ERROR("10021"),

    GLOBAL_TRANS_ROLLBACK_ERROR("10022"),

    PARAM_MISSING_ERROR("10023");

    private String errorCode;

    ClientErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
