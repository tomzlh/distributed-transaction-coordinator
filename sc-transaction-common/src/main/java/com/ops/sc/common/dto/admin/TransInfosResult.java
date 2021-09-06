package com.ops.sc.common.dto.admin;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;
import lombok.Data;

@Data
public class TransInfosResult extends ResponseResult {
    @JSONField(name = "Total")
    private Integer total;

    @JSONField(name = "TransInfos")
    private List<TransInfoDTO> transInfoList;

    public TransInfosResult(Integer total, List<TransInfoDTO> transInfoList) {
        super(TransactionResponseCode.SUCCESS);
        this.total = total;
        this.transInfoList = transInfoList;
    }


}
