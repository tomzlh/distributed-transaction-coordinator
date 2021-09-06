package com.ops.sc.server.service;

import java.util.Date;
import java.util.List;

import com.ops.sc.common.enums.CallBackType;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.enums.ExecutionResult;
import com.ops.sc.common.model.TransBranchInfo;
import com.ops.sc.common.store.ScBranchRecord;


public interface BranchTransService {

    void delete(Long tid);

    /**
     * 判断分支事务在try阶段是否超时
     *
     * @param transBranchInfo
     * @return
     */
    Boolean isBranchTransTryTimeout(ScBranchRecord transBranchInfo);

    List<ScBranchRecord> getTransBranchInfoList(Long tid);

    void save(ScBranchRecord transBranchInfo);

    void save(List<ScBranchRecord> transBranchInfoList);

    int updateStatusByBids(List<Long> bids, Integer toStatus,Date modifyTime);


    void updateFailBranchTransRetryCountAndStatus(ScBranchRecord transBranchBase);

    Boolean isNeedExecute(ScBranchRecord transBranchInfo, TransStatus globalTransStatus, Long singleTimeout);

    ExecutionResult executeBranchTrans(ScBranchRecord transBranchInfo, CallBackType callBackType);

    int updateStatusById(Long id, Integer status,Integer retryCount, Date modifyTime);


    int updateRetryCount(Long id, Integer retryCount);

    int updateLocalBranch(Long id, Integer retryCount, Integer status, Date modifyTime, Date endTime);

    ScBranchRecord findByTidAndBid(Long tid, Long branchId);

    int updateStatusByBidAndStatus(Long branchId, List<Integer> fromStatus, Integer toStatus);

}
