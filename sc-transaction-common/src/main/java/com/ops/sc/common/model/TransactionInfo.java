package com.ops.sc.common.model;

import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.trans.CommonTransInfo;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@ToString
public class TransactionInfo extends CommonTransInfo {


    protected Long tid; // 主键ID

    protected String groupId; // 事务分组ID

    protected String businessId; // 全局事务业务ID

    protected Integer status; // 全局事务状态

    protected String callerIp; // 全局事务发起者IP地址

    protected Long timeout; // 全局事务超时时间

    protected Integer timeoutType;

    protected String appName; // 应用名称

    protected TransMode transMode; //事务模型 TCC/FMT/XA/SAGA

    protected Integer callbackStrategy; // 回调策略

    protected Integer retryCount; // 冲正的次数

    protected String dataSource; // fmt模式下，回调使用

    protected Date createTime;

    protected Date modifyTime;

    protected Date endTime; // 全局事务结束时间

    protected int callInParallel;

    protected List<TransBranchInfo> branchTransactionList;

    protected StreamObserver streamObserver;


}
