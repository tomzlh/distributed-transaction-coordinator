package com.ops.sc.compensator.dao;

import com.ops.sc.common.bean.TransInfoQueryParams;
import com.ops.sc.common.store.ScTransRecord;

import java.util.List;


public interface TransInfoDao {
    /**
     * 插入transactionInfo数据
     *
     */
    void save(ScTransRecord scTransRecord);

    void delete(Long tid);


    List<ScTransRecord> findByConditions(TransInfoQueryParams transInfoQueryParams);


    int getTotalCountByConditions(TransInfoQueryParams transInfoQueryParams);


    ScTransRecord findById(Long id);


    ScTransRecord findByTid(Long tid);


    List<ScTransRecord> findByStatus(List<Integer> statusList);


    int updateStatusByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus, Integer retryCount);


    int updateStatusAndEndTimeById(Long id, Integer status);


    int updateRetryCount(Long id, Integer retryCount);


    int getTransCountByGroupAndStatus(List<String> groupIdList, List<Integer> statusList);


    int updateStatusAndInitRetryCount(Long id, Integer fromStatus, Integer toStatus);
}
