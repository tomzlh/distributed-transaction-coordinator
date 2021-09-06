package com.ops.sc.common.model;

import lombok.Data;

import java.util.Date;


@Data
public class TransErrorInfo {
    private Long id;

    private Long tid;

    private Long branchId;

    private Integer errorType;

    private String errorDetail;

    private Date createTime;

    private Date modifyTime;


}
