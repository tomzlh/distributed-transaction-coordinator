package com.ops.sc.common.enums;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Optional;


public enum TransactionResponseCode {
    SUCCESS("Success", 200, "error.success"),

    INTERNAL_ERROR("InternalError", 500, "error.internalError"),

    PARAM_IS_REQUIRED("ParamIsRequired", 400, "error.paramIsRequired"),

    GROUP_NOT_EXIST("GroupNotExist", 400, "error.groupNotExist"),

    TRANSACTION_IS_EXIST("TransactionIsExist", 400, "error.transactionIsExist"),

    PARAM_INVALID("ParamInvalid", 400, "error.paramInvalid"),

    TRANS_NOT_EXIST("TransNotExit", 400, "error.transNotExist"),

    GLOBAL_TRANS_OPERATE_ILLEGAL("GlobalTransOperateIllegal", 400, "error.globalTransOperateIllegal"),

    BRANCH_TRANS_NOT_EXIST("BranchTransNotExist", 400, "error.branchTransNotExist"),

    BRANCH_TRANS_OPERATE_ILLEGAL("BranchTransOperateIllegal", 400, "error.branchTransOperateIllegal"),


    MQ_ALREADY_EXIST("mqAlreadyExist", 400, "error.mqAlreadyExist"),

    TRANS_MSG_NOT_EXIST("TransMsgNotExist", 400, "error.msgNotExist"),

    CALL_BRANCH_ERROR("CallBranchError", 408, "error.callBranchError"),

    LOCK_CONFLICT("LockConflict", 409, "error.lockConflict"),

    AUTH_FAILED("authFailed", 410, "error.AuthFailed"),

    TRANSACTION_PROCESS_FAILED("TransactionProcessFailed", 901, "error.transactionProcessFailed"),

    TRANSACTION_PROCESS_SUCCEED("TransactionProcessSucceed", 902, "error.transactionProcessSucceed"),

    TRANSACTION_PROCESS_RETRY("TransactionProcessRetry", 903, "error.transactionProcessRetry"),

    TRANSACTION_ROLLBACK_FAILED("TransactionRollbackFailed", 904, "error.transactionRollbackFailed"),

    BRANCH_PREPARE_FAILED("BranchPrepareFailed", 905, "error.branchPrepareFailed"),

    BRANCH_COMMIT_FAILED("BranchCommitFailed", 906, "error.branchCommitFailed"),

    BRANCH_ROLLBACK_FAILED("BranchRollbackFailed", 907, "error.branchRollbackFailed"),

    NOT_SUPPORTED_MODE("NotSupportedMode", 909, "error.notSupportedMode"),

    LOCAL_DATABASE_FAILED("LocalDatabaseFailed",909, "error.localDatabaseFailed"),

    NO_SERVER_AVAILABLE("NoServerAvailable",910, "error.noServerAvailable");

    @Setter
    @Getter
    private String code;
    @Setter
    @Getter
    private Integer httpCode;
    @Setter
    @Getter
    private String descKey;

    TransactionResponseCode(String code, Integer httpCode, String descKey) {
        this.code = code;
        this.httpCode = httpCode;
        this.descKey = descKey;
    }

    public static TransactionResponseCode getErrorCodeEnum(String code) {
        Optional<TransactionResponseCode> errorCodeEnum = Arrays.stream(TransactionResponseCode.values())
                .filter(codeEnum -> codeEnum.getCode().equals(code)).findAny();
        return errorCodeEnum.orElseThrow(IllegalArgumentException::new);
    }

    public boolean isSucceed() {
        return SUCCESS.code.equalsIgnoreCase(this.code);
    }



}
