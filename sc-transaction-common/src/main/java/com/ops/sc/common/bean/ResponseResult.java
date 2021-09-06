package com.ops.sc.common.bean;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.enums.TransactionResponseCode;
import lombok.Getter;
import lombok.Setter;


public class ResponseResult implements Serializable {
    private static final long serialVersionUID = 3061424061444688372L;

    @Getter
    @Setter
    @JSONField(serialize = false)
    private String[] errorArgs;

    @Getter
    @Setter
    @JSONField(serialize = false)
    private TransactionResponseCode error;

    @Getter
    @Setter
    @JSONField(name = "Message")
    private String message;

    @Getter
    @Setter
    @JSONField(name = "Code")
    private String code;

    @Getter
    @Setter
    @JSONField(name = "BusinessId")
    private String businessId;

    public ResponseResult() {
    }

    public ResponseResult(TransactionResponseCode error, String... errorArgs) {
        this.error = error;
        this.errorArgs = errorArgs;
        this.code = error.getCode();
    }

    public static ResponseResult returnResult(TransactionResponseCode transactionResponseCode, String... args) {
        return new ResponseResult(transactionResponseCode, args);
    }

    public static ResponseResult returnSuccess() {
        return returnResult(TransactionResponseCode.SUCCESS);
    }

    @JSONField(serialize = false)
    public Boolean isSuccess() {
        return TransactionResponseCode.SUCCESS.getCode().equalsIgnoreCase(code);
    }



}
