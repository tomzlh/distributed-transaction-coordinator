package com.ops.sc.common.dto.admin;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;


public class CallBackErrorInfoResult extends ResponseResult {

    @JSONField(name = "ErrorInfo")
    private String callBackErrorInfo;

    public CallBackErrorInfoResult(String callBackErrorInfo) {
        super(TransactionResponseCode.SUCCESS);
        this.callBackErrorInfo = callBackErrorInfo;
    }

    public String getCallBackErrorInfo() {
        return callBackErrorInfo;
    }

    public void setCallBackErrorInfo(String callBackErrorInfo) {
        this.callBackErrorInfo = callBackErrorInfo;
    }
}
