package com.ops.sc.ta.dao;


import com.ops.sc.common.model.BranchInfo;

import java.sql.SQLException;
import java.util.List;


public interface BranchDao {

    void insert(BranchInfo branchInfo) throws SQLException;

    BranchInfo findByTidAndBranchId(Long tid, Long bid) throws SQLException;

    List<BranchInfo> findByStatusList(List<Integer> statusList) throws SQLException;

    int updateStatus(Long tid, Long bid, Integer fromStatus, Integer toStatus) throws SQLException;

    void delete(Long tid, Long bid) throws SQLException;

    int updateRetryCount(long id, int retryCount) throws SQLException;

    int updateModifyTime(long id, long modifyTime) throws SQLException;

    int updateStatusAndEndTimeById(long id, Integer status, long endTime, long modifyTime) throws SQLException;

}
