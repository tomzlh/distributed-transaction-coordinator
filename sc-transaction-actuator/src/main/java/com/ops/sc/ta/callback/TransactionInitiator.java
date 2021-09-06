package com.ops.sc.ta.callback;


import com.ops.sc.common.enums.GlobalTransStatus;

import java.sql.SQLException;


public interface TransactionInitiator {
    GlobalTransStatus checkBack(Long tid, String dataSource) throws SQLException;
}
