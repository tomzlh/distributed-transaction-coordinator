package com.ops.sc.common.store;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class ScTransRecord {

    private Long id;

    protected Long tid;

    protected String groupId;

    protected String businessId;

    protected Integer status;

    protected String callerIp;

    protected Long timeout;

    protected Integer timeoutType;

    protected String appName;

    protected String transMode;

    protected String eventType;

    protected Integer callbackStrategy;

    protected Integer retryCount;

    protected String dataSource;

    protected Date createTime;

    protected Date modifyTime;

    protected Date endTime; // 全局事务结束时间

    protected int callInParallel;

    protected String rollBackInfo;

    protected List<ScBranchRecord> branchTransactionList;


}
