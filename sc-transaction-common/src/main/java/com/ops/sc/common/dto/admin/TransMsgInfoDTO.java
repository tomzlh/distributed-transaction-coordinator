package com.ops.sc.common.dto.admin;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;


public class TransMsgInfoDTO extends ResponseResult {

    @JSONField(name = "MqName")
    private String mqName;

    @JSONField(name = "MqMetaData")
    private String mqMetaData;

    public TransMsgInfoDTO(String mqName, String mqMetaData) {
        super(TransactionResponseCode.SUCCESS);
        this.mqName = mqName;
        this.mqMetaData = mqMetaData;
    }

    public String getMqName() {
        return mqName;
    }

    public void setMqName(String mqName) {
        this.mqName = mqName;
    }

    public String getMqMetaData() {
        return mqMetaData;
    }

    public void setMqMetaData(String mqMetaData) {
        this.mqMetaData = mqMetaData;
    }

}
