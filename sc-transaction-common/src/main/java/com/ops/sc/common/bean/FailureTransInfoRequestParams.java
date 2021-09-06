package com.ops.sc.common.bean;

import lombok.Data;

import java.util.Date;

@Data
public class FailureTransInfoRequestParams {

    private String tenantId;

    private Date searchTimeStart;

    private Date searchTimeEnd;

    private Integer limit;

    private String transGroupName; // 事务分组名称

    private String appName;


}
