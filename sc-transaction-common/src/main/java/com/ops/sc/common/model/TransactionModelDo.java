package com.ops.sc.common.model;

import lombok.Data;

import java.util.Date;

@Data
public class TransactionModelDo {

    private Long id;

    private String transCode;

    private String transGroupId;

    private String transMode;

    private String transactionName;

    private String callMode;

    private String modelName;

    private long timeout;

    private int timeoutType;

    private Date createTime;

    private Date updateTime;

    private String remark;

    private int isInvalid;
}
