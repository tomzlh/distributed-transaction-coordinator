package com.ops.sc.ta.clone.rollback;

import com.ops.sc.core.clone.RollbackItem;
import com.ops.sc.ta.clone.enums.SqlType;

import java.sql.Connection;
import java.sql.SQLException;


public interface ImageRecover {

    SqlType getSqlType();

    void rollbackBeforeImage(RollbackItem item, Connection connection) throws SQLException;

    boolean checkAfterImage(RollbackItem item, Connection connection);
}
