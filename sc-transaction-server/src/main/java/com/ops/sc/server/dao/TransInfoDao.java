package com.ops.sc.server.dao;

import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.bean.TransInfoQueryParams;

import java.util.List;


public interface TransInfoDao {
    /**
     * 插入transactionInfo数据
     *
     * @param scTransRecord
     */
    void save(ScTransRecord scTransRecord);

    void delete(Long tid);

    /**
     * 通过transInfoQueryParams批量获取transInfo
     *
     * @param transInfoQueryParams
     * @return
     */
    List<ScTransRecord> findByConditions(TransInfoQueryParams transInfoQueryParams);

    /**
     * 通过transInfoQueryParams获取符合条件的全局事务的总数量
     *
     * @param transInfoQueryParams
     * @return
     */
    int getTotalCountByConditions(TransInfoQueryParams transInfoQueryParams);

    /**
     * 通过数据库主键id获取transInfo
     *
     * @param id
     * @return
     */
    ScTransRecord findById(Long id);


    ScTransRecord findByBusinessId(String businessId);

    /**
     * 通过tid获取transInfo
     *
     * @param tid
     * @return
     */
    ScTransRecord findByTid(Long tid);


    List<ScTransRecord> findByStatus(List<Integer> statusList);

    /**
     * 通过fromStatus修改全局事务状态
     *
     * @param tid
     * @param fromStatus
     * @param toStatus
     * @return
     */
    int updateStatusByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus);

    int updateStatusRetryCountByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus, Integer retryCount);


    int updateStatusByTids(List<Long> tids, Integer status);


    int updateStatusAndEndTimeById(Long id, Integer status);

    /**
     * 全局事务回查retryCount+1
     *
     * @param id
     * @param retryCount
     * @return
     */
    int updateRetryCount(Long id, Integer retryCount);

    /**
     * 通过状态list获取全局事务数量
     *
     * @param statusList
     * @return
     */
    int getTransCountByGroupAndStatus(List<String> groupIdList, List<Integer> statusList);

    /**
     * 修改全局事务状态并且置零retryCount
     *
     * @param id
     * @param fromStatus
     * @param toStatus
     */
    int updateStatusAndInitRetryCount(Long id, Integer fromStatus, Integer toStatus);
}
