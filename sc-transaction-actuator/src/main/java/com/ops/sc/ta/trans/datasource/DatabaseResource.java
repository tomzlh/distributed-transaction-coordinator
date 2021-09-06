package com.ops.sc.ta.trans.datasource;

import com.ops.sc.common.exception.ScClientException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public interface DatabaseResource extends Connection {
    ScDataSource getDataSource();

    Connection getOriginalConnection();

    void begin() throws SQLException;

    void end() throws SQLException;

    DatabaseMetaData getDBMetaData() throws ScClientException,SQLException;

}
