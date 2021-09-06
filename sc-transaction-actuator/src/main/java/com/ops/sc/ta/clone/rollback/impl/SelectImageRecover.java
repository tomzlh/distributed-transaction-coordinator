package com.ops.sc.ta.clone.rollback.impl;

import com.ops.sc.core.clone.RollbackItem;
import com.ops.sc.ta.clone.enums.SqlType;
import com.ops.sc.ta.clone.rollback.BaseRecover;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component("selectImageRecover")
public class SelectImageRecover extends BaseRecover {
    @Override
    public SqlType getSqlType() {
        return SqlType.SELECT;
    }

    @Override
    public void rollbackBeforeImage(RollbackItem item, Connection connection) {

    }

    @Override
    public boolean checkAfterImage(RollbackItem item, Connection connection) {
        return true;
    }
}
