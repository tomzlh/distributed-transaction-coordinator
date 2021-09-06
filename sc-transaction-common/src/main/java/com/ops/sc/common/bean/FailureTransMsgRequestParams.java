package com.ops.sc.common.bean;

import lombok.Data;

import java.util.Date;

/**
 * 异常全局事务消息查询参数
 *
 */
@Data
public class FailureTransMsgRequestParams {

    private String tenantId;

    private String projectId;

    private Long idLessThan;

    private Date searchTimeStart;

    private Date searchTimeEnd;

    private Integer limit;

    private String transGroupName;


}
