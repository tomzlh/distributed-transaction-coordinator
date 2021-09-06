package com.ops.sc.mybatis.mapper;

import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.bean.TransInfoQueryParams;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


public interface TransInfoMapper {

    void save(ScTransRecord transactionInfo);

    int update(ScTransRecord transactionInfo);

    List<ScTransRecord> findByConditions(TransInfoQueryParams transInfoQueryParams);

    int getCountByConditions(TransInfoQueryParams transInfoQueryParams);

    ScTransRecord findById(@Param("id") Long id);

    ScTransRecord findByBusinessId(@Param("businessId") String businessId);

    ScTransRecord findByTid(@Param("tid") Long tid);

    void delete(@Param("tid") Long tid);

    int updateStatusByTid(@Param("tid") Long tid, @Param("status") Integer status,
                          @Param("modifyTime") Date modifyTime);

    List<ScTransRecord> findByStatus(@Param("statusList") List<Integer> statusList);

    int updateStatusByTidAndStatus(@Param("tid") Long tid, @Param("fromStatus") Integer fromStatus,
                                   @Param("toStatus") Integer toStatus, @Param("modifyTime") Date modifyTime);

    int updateStatusByTids(@Param("tids") List<Long> tids, @Param("status") Integer status, @Param("modifyTime") Date modifyTime);

    int updateStatusAndRetryCount(@Param("tid") Long tid, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("retryCount") Integer retryCount, @Param("now") Date now);

}
