package com.ops.sc.common.dto.admin;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;


public class BranchInfosResult extends ResponseResult {

    @JSONField(name = "TransBranchInfos")
    private List<BranchInfoDTO> branchInfoList;

    public BranchInfosResult(List<BranchInfoDTO> branchInfoList) {
        super(TransactionResponseCode.SUCCESS);
        this.branchInfoList = branchInfoList;
    }

    public List<BranchInfoDTO> getBranchInfoList() {
        return branchInfoList;
    }

    public void setBranchInfoList(List<BranchInfoDTO> branchInfoList) {
        this.branchInfoList = branchInfoList;
    }
}
