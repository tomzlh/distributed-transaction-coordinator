package com.ops.sc.common.dto.admin;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;


@Data
public class TransInfoDTO {

    @JSONField(name = "Id")
    private Long id;

    @JSONField(name = "Tid")
    private Long tid;

    @JSONField(name = "BizId")
    private String bizId;

    @JSONField(name = "Status")
    private Integer status;

    @JSONField(name = "TransGroupName")
    private String transGroupName;

    @JSONField(name = "CallerIp")
    private String callerIp;

    @JSONField(name = "CreateTime")
    private String createTime;

    @JSONField(name = "EndTime")
    private String endTime;

    @JSONField(name = "TransName")
    private String transName;

    @JSONField(name = "AppName")
    private String appName;
    @JSONField(name = "TransType")
    private int transType;
}
