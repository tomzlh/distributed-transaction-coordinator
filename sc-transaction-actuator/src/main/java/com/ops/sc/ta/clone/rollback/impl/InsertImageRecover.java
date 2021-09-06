package com.ops.sc.ta.clone.rollback.impl;

import com.ops.sc.core.clone.RollbackItem;
import com.ops.sc.ta.clone.enums.SqlType;
import com.ops.sc.ta.clone.rollback.BaseRecover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component("insertImageRecover")
public class InsertImageRecover extends BaseRecover {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertImageRecover.class);

    @Override
    public SqlType getSqlType() {
        return SqlType.INSERT;
    }

    @Override
    public void rollbackBeforeImage(RollbackItem item, Connection connection) throws SQLException {
        String sql = "delete from " + item.getTableName() + item.getSqlIdWhere();
        LOGGER.debug("Rollback insertImage. sql: {}", sql);
        try (Statement sm = connection.createStatement()) {
            sm.executeUpdate(sql);
        }
    }
}
