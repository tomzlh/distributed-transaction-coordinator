package com.ops.sc.ta.clone.resolver.impl;


import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.ta.clone.dto.SqlInfo;
import com.ops.sc.ta.clone.resolver.ParentParser;
import com.ops.sc.ta.trans.datasource.DatabaseResource;

import java.sql.SQLException;


public class UpdateImageParser extends ParentParser {

    public UpdateImageParser(SqlInfo sqlInfo) {
        super(sqlInfo);
    }

    @Override
    public void genBeforeImage(DatabaseResource databaseResource) throws ScClientException,SQLException {
        beforeImage = genImageAndSetPkAndValue(databaseResource);
    }

    @Override
    public void genAfterImage(DatabaseResource databaseResource) throws ScClientException,SQLException {
        afterImage = genImageAndSetPkAndValue(databaseResource);
    }

    @Override
    public String getImageSqlWhere() {
        return sqlInfo.getSqlWhere();
    }
}
