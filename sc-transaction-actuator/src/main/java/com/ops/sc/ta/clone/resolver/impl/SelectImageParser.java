package com.ops.sc.ta.clone.resolver.impl;


import com.ops.sc.core.clone.ImageInfo;
import com.ops.sc.ta.clone.dto.SqlInfo;
import com.ops.sc.ta.clone.resolver.ParentParser;
import com.ops.sc.ta.trans.datasource.DatabaseResource;

import java.sql.SQLException;


public class SelectImageParser extends ParentParser {

    public SelectImageParser(SqlInfo sqlInfo) {
        super(sqlInfo);
    }

    @Override
    public void genBeforeImage(DatabaseResource databaseResource) throws SQLException {

        beforeImage = new ImageInfo();
    }

    @Override
    public void genAfterImage(DatabaseResource databaseResource) {
        afterImage = new ImageInfo();
    }

    @Override
    public String getImageSqlWhere() {
        return sqlInfo.getSqlWhere();
    }
}
