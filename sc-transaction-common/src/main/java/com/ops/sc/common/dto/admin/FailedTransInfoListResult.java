package com.ops.sc.common.dto.admin;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;
import lombok.Data;

@Data
public class FailedTransInfoListResult extends ResponseResult {
    @JSONField(name = "Total")
    private Integer total;

    @JSONField(name = "FailedTransInfos")
    private List<FailedTransInfoDTO> faileTransInfoDTOList;

    public FailedTransInfoListResult(Integer total, List<FailedTransInfoDTO> faileTransInfoDTOList) {
        super(TransactionResponseCode.SUCCESS);
        this.total = total;
        this.faileTransInfoDTOList = faileTransInfoDTOList;
    }

    public FailedTransInfoListResult() {
        super(TransactionResponseCode.SUCCESS);
        faileTransInfoDTOList = Lists.newArrayList();
    }

}
