package com.ops.sc.compensator.service;

import com.ops.sc.common.bean.TransInfoQueryParams;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.compensator.grpc.sync.CompensatorGrpcSyncClient;

import java.util.List;


public interface TransactionOperationService {

    /**
     * 全局事务try阶段是否timeout
     */
    Boolean isGlobalTransTryTimeout(ScTransRecord scTransRecord);



    /**
     * 运维接口， 对try-timeout的全局tcc事务进行回查，如果查到结果，则进行异步confirm/cancel操作
     *
     * @param scTransRecord
     */
    void checkAbnormalGlobalTrans(ScTransRecord scTransRecord, CompensatorGrpcSyncClient compensatorGrpcSyncClient) throws RpcException;





    List<ScTransRecord> findByStatus(List<Integer> statusList);

    List<ScTransRecord> find(TransInfoQueryParams queryParams);

    /**
     * 通过fromStatus修改全局事务状态
     *
     * @param tid
     * @param fromStatus
     * @param toStatus
     * @return
     */
    int updateStatusByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus,int retryCount);

    int updateStatusByBidAndStatus(Long bid, Integer toStatus,int retryCount);

    void deleteBranch(Long tid);

    void deleteGlobal(Long tid);

    boolean isBranchTransTryTimeout(ScBranchRecord scBranchRecord);
}
