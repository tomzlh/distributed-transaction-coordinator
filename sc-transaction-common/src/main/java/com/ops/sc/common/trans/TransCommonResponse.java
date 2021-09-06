package com.ops.sc.common.trans;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class TransCommonResponse {

    private Status status;

    private String errorCode;

    private String errorMsg;

    private String businessId;

    private Long tid;

    private String branchId;


    public TransCommonResponse notExecute() {
        this.status=Status.NOT_EXECUTED;
        return this;
    }

    public TransCommonResponse success() {
        this.status=Status.SUCCESS;
        return this;
    }

    public TransCommonResponse failed() {
        this.status=Status.FAILED;
        return this;
    }

    public boolean isSuccess() {
        return status.equals(Status.SUCCESS);
    }

    public boolean isNotExecute() {
        return status.equals(Status.NOT_EXECUTED);
    }

    public TransCommonResponse setErrorInfo(String errorInfo) {
        this.errorMsg = errorInfo;
        return this;
    }


    public enum Status {
        SUCCESS,

        FAILED,

        ROLLEDBACK,

        NOT_EXECUTED
    }

}
