package com.ops.sc.common.dto.admin;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;
import lombok.Data;

@Data
public class TransGroupInfosResult extends ResponseResult {
    @JSONField(name = "Total")
    private Integer total;

    @JSONField(name = "TransGroupInfos")
    private List<TransGroupInfoDTO> transGroupList;

    public TransGroupInfosResult(Integer total, List<TransGroupInfoDTO> transGroupList) {
        super(TransactionResponseCode.SUCCESS);
        this.total = total;
        this.transGroupList = transGroupList;
    }

}
