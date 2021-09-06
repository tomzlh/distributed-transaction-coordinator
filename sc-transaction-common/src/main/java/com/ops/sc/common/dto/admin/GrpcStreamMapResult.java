package com.ops.sc.common.dto.admin;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;


public class GrpcStreamMapResult extends ResponseResult {

    @JSONField(name = "LocalStreamList")
    private Map<String, List<String>> localStreamList;

    public GrpcStreamMapResult(Map<String, List<String>> localStreamList) {
        super(TransactionResponseCode.SUCCESS);
        this.localStreamList = localStreamList;
    }

    public Map<String, List<String>> getLocalStreamList() {
        return localStreamList;
    }

    public void setLocalStreamList(Map<String, List<String>> localStreamList) {
        this.localStreamList = localStreamList;
    }
}
