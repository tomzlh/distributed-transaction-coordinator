package com.ops.sc.common.model;

import lombok.Data;

import java.util.Date;

@Data
public class ModelDetailDo {

    private Long id;

    private Long modelId;

    private String modelBranchName;

    private String branchName;

    private int retryRequired;

    private int retryCount;

    private long timeout;

    private String timeoutType;

    private String parentNames;

    private int hasParent;

    private String requestParams;

    private String externalParams;

    private String resParams;

    private Date createTime;

    private Date updateTime;

    private int isInvalid;
}
