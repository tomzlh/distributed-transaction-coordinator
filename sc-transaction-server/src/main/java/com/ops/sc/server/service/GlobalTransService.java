package com.ops.sc.server.service;

import java.util.List;

import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.bean.TransInfoQueryParams;


public interface GlobalTransService {

    void processBranchTransTryStatus(ScTransRecord transactionInfo, List<ScBranchRecord> transBranchInfoList);

    /**
     * 全局事务try阶段是否timeout
     */
    Boolean isGlobalTransTryTimeout(ScTransRecord transactionInfo);

    /**
     * 全局事务状态回查
     *
     * @param transactionInfo
     */
    TransStatus globalTransStatusCheckBack(ScTransRecord transactionInfo);


    void checkBackTimeoutGlobalTrans(ScTransRecord transactionInfo);

    void save(ScTransRecord transactionInfo);

    void saveTransAndBranchTrans(ScTransRecord scTransRecord);

    void updateStatusAndEndTimeById(Long id, Integer status);

    ScTransRecord getByTid(Long tid);

    ScTransRecord getByBusinessId(String businessId);

    void delete(Long tid);

    List<ScTransRecord> findByStatus(List<Integer> statusList);

    List<ScTransRecord> find(TransInfoQueryParams queryParams);


    int updateStatusByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus);

    int updateStatusByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus,Integer retryCount);

    int updateStatusByTids(List<Long> tids, Integer toStatus);

    int updateRetryCount(Long id, Integer retryCount);
}
