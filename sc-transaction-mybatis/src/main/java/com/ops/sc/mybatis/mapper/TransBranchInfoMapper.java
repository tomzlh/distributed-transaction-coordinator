package com.ops.sc.mybatis.mapper;

import java.util.Date;
import java.util.List;

import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.bean.TransBranchInfoQueryParams;
import org.apache.ibatis.annotations.Param;


public interface TransBranchInfoMapper {

    void save(ScBranchRecord transBranchInfo);

    void batchSave(@Param("transBranchInfos") List<ScBranchRecord> transBranchInfos);

    void delete(@Param("tid") Long tid);

    Integer update(ScBranchRecord transBranchInfo);

    List<ScBranchRecord> findByConditions(TransBranchInfoQueryParams transBranchInfoQueryParams);

    ScBranchRecord findById(@Param("id") Long id);

    ScBranchRecord findByTidAndBranchId(@Param("tid") Long tid, @Param("branchId") Long branchId);

    int updateByBranchIdAndStatus(@Param("branchId") Long branchId,
            @Param("statusList") List<Integer> fromStatus, @Param("toStatus") Integer toStatus, @Param("now") Date now);

    int updateByBranchId(@Param("bid") Long bid, @Param("status") Integer status, @Param("retryCount") Integer retryCount,
                         @Param("modifyTime") Date modifyTime);

    int updateStatusByBranchId(@Param("branchId") Long branchId,
                               @Param("status") Integer status, @Param("retryCount") Integer retryCount, @Param("now") Date now);

    int getBranchCountByGroupAndGlobalStatus(@Param("groupIdList") List<String> groupIdList,
                                             @Param("statusList") List<Integer> statusList, @Param("createTimeStart") Date createTimeStart);

    int getBranchCountByGroupAndStatus(@Param("groupIdList") List<String> groupIdList,
                                       @Param("statusList") List<Integer> statusList, @Param("createTimeStart") Date createTimeStart);

    int updateStatusAndRetryCount(@Param("id") Long id, @Param("statusList") List<Integer> fromStatus,
            @Param("toStatus") Integer toStatus, @Param("retryCount") Integer retryCount, @Param("now") Date now);

    int updateStatusByBids(@Param("bids") List<Long> bids, @Param("status") Integer status, @Param("modifyTime") Date modifyTime);

}
