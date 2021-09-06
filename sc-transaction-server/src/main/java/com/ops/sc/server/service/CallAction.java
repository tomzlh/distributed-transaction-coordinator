package com.ops.sc.server.service;

import com.ops.sc.common.enums.CallBackType;
import com.ops.sc.common.enums.ExecutionResult;
import com.ops.sc.common.enums.GlobalTransStatus;
import com.ops.sc.common.exception.ResourceException;
import com.ops.sc.common.model.TransBranchInfo;
import com.ops.sc.common.model.TransactionInfo;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;

public interface CallAction {

    /**
     * 分支事务confirm/cancel调用
     *
     * @param transBranchInfo
     * @param callBackType
     * @return
     */
    ExecutionResult branchExecute(ScBranchRecord transBranchInfo, CallBackType callBackType);

    /**
     * 全局事务回查
     *
     * @param transInfo
     * @return trying/try-success/try-fail
     */
    GlobalTransStatus globalCheckBackExecute(ScTransRecord transInfo);

    /**
     * 全局事务上报成功后通知客户端删除sc_log记录
     *
     * @param transInfo
     */
    void notifyClient(TransactionInfo transInfo);

    /**
     * 同库模式下通知客户端处理异常事务
     */
    void localCompensate() throws ResourceException;

    /**
     * 本地模式手动运维重试分支Callback
     *
     * @param tid
     * @param branchId
     * @param appName
     * @return
     */
    boolean retryFailLocalBranch(Long tid, String branchId, String appName);

    /**
     * 本地模式手动运维Callback
     *
     * @param tid
     * @param branchId
     * @param appName
     * @return
     */
    boolean cancelTimeoutLocalBranch(Long tid, String branchId, String appName);

}
