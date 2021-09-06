package com.ops.sc.ta.clone.resolver.impl;


import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.core.clone.ImageInfo;
import com.ops.sc.ta.clone.dto.SqlInfo;
import com.ops.sc.ta.clone.resolver.ParentParser;
import com.ops.sc.ta.trans.datasource.DatabaseResource;

import java.sql.SQLException;


public class InsertImageParser extends ParentParser {
    public InsertImageParser(SqlInfo sqlInfo) {
        super(sqlInfo);
    }

    @Override
    public void genBeforeImage(DatabaseResource databaseResource) {
        beforeImage = new ImageInfo();
    }

    @Override
    public void genAfterImage(DatabaseResource databaseResource) throws ScClientException,SQLException {
        afterImage = genImageAndSetPkAndValue(databaseResource);
    }

    @Override
    public String getImageSqlWhere() {
        return sqlInfo.getSqlWherePrimaryKey();
    }
}
