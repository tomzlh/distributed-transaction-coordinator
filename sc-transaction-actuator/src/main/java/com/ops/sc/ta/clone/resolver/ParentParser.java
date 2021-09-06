package com.ops.sc.ta.clone.resolver;


import com.ops.sc.common.enums.DatabaseType;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.utils.CommonUtils;
import com.ops.sc.core.clone.ImageInfo;
import com.ops.sc.core.clone.RollbackItem;
import com.ops.sc.core.clone.RollbackLine;
import com.ops.sc.core.clone.RollbackLineField;
import com.ops.sc.ta.clone.dto.*;
import com.ops.sc.ta.trans.datasource.DatabaseResource;
import com.ops.sc.ta.trans.datasource.ScDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public abstract class ParentParser implements ImageParser {

    protected ImageInfo beforeImage;

    protected ImageInfo afterImage;

    protected SqlInfo sqlInfo;

    public ParentParser(SqlInfo sqlInfo) {
        this.sqlInfo = sqlInfo;
    }

    @Override
    public SqlInfo getSqlInfo() {
        return sqlInfo;
    }

    public abstract String getImageSqlWhere();

    @Override
    public void setSqlInfoPrimaryKey(String primaryKey) {
        sqlInfo.setPrimaryKey(primaryKey);
    }

    @Override
    public RollbackItem genRollbackItem() {
        RollbackItem result = new RollbackItem();
        result.setBeforeImage(beforeImage);
        result.setAfterImage(afterImage);
        result.setPrimaryKey(sqlInfo.getPrimaryKey());
        result.setSqlSelect(sqlInfo.getSqlSelect());
        result.setSqlIdWhere(sqlInfo.getSqlWherePrimaryKey());
        result.setSqlRawWhere(sqlInfo.getSqlWhere());
        result.setSql(sqlInfo.getSql());
        result.setSqlType(sqlInfo.getSqlType());
        result.setTableName(sqlInfo.getTableName());

        return result;
    }

    protected final ImageInfo genImageAndSetPkAndValue(DatabaseResource databaseResource) throws ScClientException,SQLException {
        List<Map<String, Object>> rowList;
        Connection connection = databaseResource.getOriginalConnection();

        try (Statement sm = connection.createStatement();
                ResultSet rs = sm.executeQuery(
                        getImageSql(databaseResource.getDataSource(), sqlInfo.getSqlType(), sqlInfo.getTableName()))) {
            rowList = CommonUtils.rsToList(rs);
        }

        List<RollbackLine> lineList = new ArrayList<>();
        Set<String> primaryKeyValueSet = new HashSet<>();

        for (Map<String, Object> row : rowList) {
            RollbackLine line = new RollbackLine();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                line.getFields().add(new RollbackLineField(entry.getKey(), entry.getValue()));
            }
            lineList.add(line);

            primaryKeyValueSet.add(row.get(sqlInfo.getPrimaryKey()).toString());

        }
        sqlInfo.setPrimaryKeyValueSet(primaryKeyValueSet);

        return new ImageInfo(lineList);
    }


    private String getImageSql(ScDataSource scDataSource, String sqlType, String tableName) throws ScClientException{
        DatabaseType dbType = scDataSource.getDbType();

        StringBuilder sb = new StringBuilder();

        sb.append(sqlInfo.getSqlSelect());

        if (getImageSqlWhere() != null) {
            sb.append(getImageSqlWhere());
        }
        sb.append(" FOR UPDATE");
        return sb.toString();
    }


}
