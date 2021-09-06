package com.ops.sc.common.dto.admin;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;


public class CreateTransGroupResult extends ResponseResult {

    @JSONField(name = "TransGroupId")
    private String transGroupId;

    public CreateTransGroupResult(String transGroupId) {
        super(TransactionResponseCode.SUCCESS);
        this.transGroupId = transGroupId;
    }

    public String getTransGroupId() {
        return transGroupId;
    }

    public void setTransGroupId(String transGroupId) {
        this.transGroupId = transGroupId;
    }
}
