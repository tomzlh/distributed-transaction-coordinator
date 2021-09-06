package com.ops.sc.ta.dao;


import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.ta.trans.datasource.DatabaseResource;


import java.sql.Connection;
import java.sql.SQLException;


public interface LogDao {

    void insert(ScTransRecord scTransRecord, DatabaseResource databaseResource) throws SQLException;

    void insert(ScTransRecord scTransRecord) throws SQLException;

    void insertXATrans(ScTransRecord scTransRecord) throws SQLException;

    ScTransRecord findInitiatorByTid(Long tid, Connection connection) throws SQLException;

    ScTransRecord findByTidAndBranchId(Long tid, Long branchId, Connection connection) throws SQLException;

    ScTransRecord findByTidAndBranchId(Long tid, Long branchId) throws SQLException;

    int updateStatus(Long tid, Long branchId, Integer status, Connection connection) throws SQLException;

    int updateStatus(Long tid, Long branchId, Integer status) throws SQLException;

    int updateInitiatorStatusByTid(Long tid, Integer status) throws SQLException;

    int updateXAInitiatorStatusByTid(Long tid, Integer status) throws SQLException;

    void delete(Long tid, Long branchId) throws SQLException;

    void delete(Long tid, Long branchId, Connection connection) throws SQLException;

}
