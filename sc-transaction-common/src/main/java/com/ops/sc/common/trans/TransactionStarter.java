package com.ops.sc.common.trans;

import java.sql.SQLException;

import com.ops.sc.common.enums.GlobalTransStatus;


public interface TransactionStarter {
    GlobalTransStatus checkBack(String tid, String dataSource) throws SQLException;
}
