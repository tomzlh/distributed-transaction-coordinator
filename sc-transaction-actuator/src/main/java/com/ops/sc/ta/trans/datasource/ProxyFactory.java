package com.ops.sc.ta.trans.datasource;

import java.sql.PreparedStatement;
import java.sql.Statement;

public interface ProxyFactory {
    Statement newStatementProxy(Statement statement, ScDataSource dataSource, DatabaseResource databaseResource);

    PreparedStatement newPreparedStatementProxy(PreparedStatement statement, String sql, ScDataSource dataSource,
            DatabaseResource databaseResource);
}
