package com.ops.sc.server.dao;

import com.ops.sc.common.store.ScTransRecord;

import java.sql.SQLException;

public interface TransactionDao {

    void saveTrans(ScTransRecord scTransRecord) throws SQLException;

    void saveXATrans(ScTransRecord scTransRecord) throws SQLException;

    int updateStatusByTid(String tid, Integer status) throws SQLException;

    ScTransRecord findTranRecord(String businessId);
}
