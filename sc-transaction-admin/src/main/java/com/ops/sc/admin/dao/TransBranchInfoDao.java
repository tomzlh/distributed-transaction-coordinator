package com.ops.sc.admin.dao;

import com.ops.sc.common.bean.TransBranchInfoQueryParams;
import com.ops.sc.common.store.ScBranchRecord;

import java.util.Date;
import java.util.List;

public interface TransBranchInfoDao {

    void save(ScBranchRecord transBranchInfo);

    void delete(Long tid);

    /**
     * 根据BranchInfoQueryParams批量获取branch
     *
     * @param transBranchInfoQueryParams
     * @return
     */
    List<ScBranchRecord> findByConditions(TransBranchInfoQueryParams transBranchInfoQueryParams);

    List<ScBranchRecord> findByTid(Long tid);


    ScBranchRecord findById(Long id);


    ScBranchRecord findByTidAndBid(Long tid, Long bid);


    int updateStatusByBranchIdAndStatus(Long tid, Long bid, List<Integer> fromStatus, Integer toStatus);

    int updateStatusByBranchId(Long bid, Integer status, Integer retryCount);


    int updateStatusAndEndTimeById(Long id, Integer status);


    int updateRetryCount(Long id, Integer retryCount);

    int updateLocalBranch(Long id, Integer retryCount, Integer status, Date modifyTime, Date endTime);

    /**
     * 获取符合statusList条件的全局事务，所包含的分支事务的数量
     *
     * @return
     */
    int getBranchTransCountByGroupAndGlobalTransStatus(List<String> groupIdList, List<Integer> statusList);

    /**
     * 获取符合statusList条件的分支事务数量
     *
     * @param statusList
     * @return
     */
    int getBranchTransCountByGroupAndStatus(List<String> groupIdList, List<Integer> statusList);


    int updateStatusAndInitRetryCount(Long id, List<Integer> fromStatus, Integer toStatus);

}
