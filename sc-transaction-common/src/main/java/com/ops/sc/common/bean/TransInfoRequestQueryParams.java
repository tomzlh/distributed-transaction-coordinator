package com.ops.sc.common.bean;

import lombok.Data;

import java.util.Date;

@Data
public class TransInfoRequestQueryParams {

    private Boolean activeTrans;

    private Integer status;

    private String transGroupName;

    private Long tid;

    private String bizId;

    private String transName;

    private String appName;

    private Date searchTimeStart;

    private Date searchTimeEnd;

    private Integer limit;

    private Integer offset;

    public TransInfoRequestQueryParams(Boolean activeTrans, Integer limit, Integer offset) {
        if (activeTrans == null || limit == null || offset == null) {
            throw new IllegalArgumentException();
        }
        this.activeTrans = activeTrans;
        this.limit = limit;
        this.offset = offset;
    }


}
