package com.ops.sc.ta.trans.datasource;

import java.sql.PreparedStatement;
import java.sql.Statement;

public class JdbcProxyFactory implements ProxyFactory {
    @Override
    public Statement newStatementProxy(Statement statement, ScDataSource scDataSource, DatabaseResource databaseResource) {
        return new JdbcStatementProxy(statement, scDataSource, databaseResource);
    }

    @Override
    public PreparedStatement newPreparedStatementProxy(PreparedStatement statement, String sql,
                                                       ScDataSource scDataSource, DatabaseResource databaseResource) {
        return new JdbcPreparedStatementProxy(statement, sql, scDataSource, databaseResource);
    }
}
