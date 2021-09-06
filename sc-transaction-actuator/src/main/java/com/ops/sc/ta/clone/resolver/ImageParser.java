package com.ops.sc.ta.clone.resolver;



import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.core.clone.RollbackItem;
import com.ops.sc.ta.clone.dto.SqlInfo;
import com.ops.sc.ta.trans.datasource.DatabaseResource;

import java.sql.SQLException;


public interface ImageParser {

    RollbackItem genRollbackItem();

    void genBeforeImage(DatabaseResource databaseResource) throws ScClientException,SQLException;

    void genAfterImage(DatabaseResource databaseResource) throws ScClientException,SQLException;

    void setSqlInfoPrimaryKey(String primaryKey);

    SqlInfo getSqlInfo();

}
