package com.ops.sc.common.dto.admin;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class BranchInfoDTO {
    @JSONField(name = "Bid")
    private Long bid;

    @JSONField(name = "Status")
    private Integer status;

    @JSONField(name = "BranchName")
    private String branchName;

    @JSONField(name = "CallerIp")
    private String callerIp;

    @JSONField(name = "BranchType")
    private Integer branchType;

    @JSONField(name = "CreateTime")
    private String createTime;

    @JSONField(name = "AppName")
    private String appName;

    @JSONField(name = "RetryCount")
    private Integer retryCount;


}
