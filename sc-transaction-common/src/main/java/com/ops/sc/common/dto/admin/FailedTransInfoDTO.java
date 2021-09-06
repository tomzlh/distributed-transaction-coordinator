package com.ops.sc.common.dto.admin;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class FailedTransInfoDTO extends TransInfoDTO {

    @JSONField(name = "Tid")
    private Long tid;

    @JSONField(name = "Status")
    private Integer status;

    @JSONField(name = "TransGroupName")
    private String transGroupName;

    @JSONField(name = "CallerIp")
    private String callerIp;

    @JSONField(name = "CreateTime")
    private String createTime;

    @JSONField(name = "Id")
    private Long id;

    @JSONField(name = "BizId")
    private String bizId;

    @JSONField(name = "TransName")
    private String transName;

    @JSONField(name = "FailedBranchTransInfos")
    private List<BranchInfoDTO> failedBranchTransList;

    public FailedTransInfoDTO() {
    }


}
