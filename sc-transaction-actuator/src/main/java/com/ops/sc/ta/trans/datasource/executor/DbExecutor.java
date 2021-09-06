package com.ops.sc.ta.trans.datasource.executor;


import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.ta.trans.datasource.BaseStatementProxy;
import com.ops.sc.ta.trans.datasource.ScDataSource;

import java.sql.Connection;
import java.sql.SQLException;



public interface DbExecutor {

    <T> T execute(BaseStatementProxy stmtProxy, String sql, StatementCallback<T> callback) throws ScClientException,SQLException;

    /**
     * 生成主键id, 通过scDataSource生成Connection并且close
     *
     * @param scDataSource
     * @return
     */
    Long generateID(ScDataSource scDataSource, String tableName) throws SQLException;

    /**
     * 通过connection直接生成主键id
     *
     * @param connection
     * @param tableName
     * @return
     */
    Long generateID(Connection connection, String tableName) throws SQLException;
}
