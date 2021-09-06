package com.ops.sc.ta.trans.datasource;


import com.ops.sc.common.enums.DatabaseType;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.ta.trans.datasource.executor.DbExecutorFactory;
import com.ops.sc.ta.trans.datasource.executor.StatementCallback;
import org.springframework.stereotype.Component;

import java.sql.SQLException;


@Component("statementTemplate")
public class StatementTemplate {

    public static <T> T execute(BaseStatementProxy stmtProxy, String sql, StatementCallback<T> callback)
            throws SQLException {
        DatabaseType dbType = stmtProxy.getScDataSource().getDbType();
        try {
            T t = DbExecutorFactory.getDbExecutor(dbType).execute(stmtProxy, sql, callback);
            return t;
        }catch (ScClientException e){
            throw new SQLException(e);
        }
    }

}
