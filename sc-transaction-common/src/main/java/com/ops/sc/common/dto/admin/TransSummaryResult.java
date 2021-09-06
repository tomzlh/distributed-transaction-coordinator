package com.ops.sc.common.dto.admin;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;
import lombok.Data;

@Data
public class TransSummaryResult extends ResponseResult {

    @JSONField(name = "ActiveTransNum")
    private Integer activeTransNum;

    @JSONField(name = "ActiveBranchNum")
    private Integer activeBranchNum;

    @JSONField(name = "AbnormalBranchNum")
    private Integer abnormalBranchNum;

    @JSONField(name = "AbnormalNum")
    private Integer abnormalTransNum;

    public TransSummaryResult(Integer activeTransNum, Integer activeBranchNum, Integer abnormalBranchNum,
                              Integer abnormalTransNum) {
        super(TransactionResponseCode.SUCCESS);
        this.activeTransNum = activeTransNum;
        this.activeBranchNum = activeBranchNum;
        this.abnormalBranchNum = abnormalBranchNum;
        this.abnormalTransNum = abnormalTransNum;
    }

    public TransSummaryResult() {
        super(TransactionResponseCode.SUCCESS);
        activeTransNum = 0;
        activeBranchNum = 0;
        abnormalTransNum = 0;
        abnormalBranchNum = 0;
    }


}
