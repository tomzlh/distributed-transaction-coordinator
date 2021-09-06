package com.ops.sc.server.dao;

import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.bean.TransBranchInfoQueryParams;

import java.util.Date;
import java.util.List;

public interface TransBranchInfoDao {

    void save(ScBranchRecord transBranchInfo);

    void save(List<ScBranchRecord> transBranchInfos);

    void delete(Long tid);


    List<ScBranchRecord> findByConditions(TransBranchInfoQueryParams transBranchInfoQueryParams);

    List<ScBranchRecord> findByTid(Long tid);


    ScBranchRecord findById(Long id);


    ScBranchRecord findByTidAndBid(Long tid, Long bid);


    int updateStatusByBranchIdAndStatus(Long bid, List<Integer> fromStatus, Integer toStatus);

    int updateStatusByBranchId(Long bid, Integer status, Integer retryCount);


    int updateByBranchId(Long bid, Integer status, Integer retryCount, Date modifyTime);


    int updateStatusAndEndTimeById(Long id, Integer status);


    int updateRetryCount(Long id, Integer retryCount);

    int updateLocalBranch(Long id, Integer retryCount, Integer status, Date modifyTime, Date endTime);


    int getBranchCountByGroupAndGlobalStatus(List<String> groupIdList, List<Integer> statusList);


    int getBranchCountByGroupAndStatus(List<String> groupIdList, List<Integer> statusList);


    int updateStatusAndRetryCount(Long id, List<Integer> fromStatus, Integer toStatus);

    int updateStatusByBids(List<Long> bids,Integer toStatus,Date modifyTime);

}
