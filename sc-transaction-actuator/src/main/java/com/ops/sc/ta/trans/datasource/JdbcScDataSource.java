package com.ops.sc.ta.trans.datasource;


import com.ops.sc.common.enums.DatabaseType;
import org.springframework.jdbc.datasource.SmartDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


public class JdbcScDataSource extends ScDataSource {

    private DataSource targetDataSource;

    public JdbcScDataSource() {
        super(new JdbcProxyFactory(), DatabaseType.getDefault());
    }

    public void setTargetDataSource(DataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
    }

    @Override
    protected Connection doGetConnection() throws SQLException {
        return targetDataSource.getConnection();
    }

    @Override
    protected Connection doGetConnection(String username, String password) throws SQLException {
        return targetDataSource.getConnection(username, password);
    }

    @Override
    protected boolean doShouldClose(Connection con) {
        if (targetDataSource instanceof SmartDataSource) {
            return ((SmartDataSource) (targetDataSource)).shouldClose(con);
        } else {
            return true;
        }
    }
}
