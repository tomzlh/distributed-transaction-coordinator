package com.ops.sc.common.store;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Data
@ToString
public class ScBranchRecord {

    private Long id;

    protected Long bid;

    protected Long tid;

    protected String businessId;

    protected String parentName;

    protected String branchParam;

    protected String externalParam;

    protected String callerIp;

    protected String branchTransName;

    protected Integer retry;

    protected Integer retryCount;

    protected Integer status;

    protected Date endTime;

    protected Date createTime;

    protected Date modifyTime;

    protected String branchName;

    protected Long timeout;

    protected Integer timeoutType;

    protected String dataSource;

    private String transactionName;

    private Integer hasParent;

    private String url;

    private Map<String,String> urlMap;

    private String returnParam;

    private String rollbackParam;

    private Integer transMode;

    private Integer orderNo;
}
