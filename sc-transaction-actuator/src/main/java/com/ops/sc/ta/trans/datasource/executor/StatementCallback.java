package com.ops.sc.ta.trans.datasource.executor;

import java.sql.SQLException;


public interface StatementCallback<T> {

    T execute() throws SQLException;
}
