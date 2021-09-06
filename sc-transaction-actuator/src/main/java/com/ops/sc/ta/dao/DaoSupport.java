package com.ops.sc.ta.dao;


import com.ops.sc.common.enums.DatabaseType;
import com.ops.sc.ta.dao.impl.BranchDaoImpl;
import com.ops.sc.ta.trans.datasource.DatabaseResource;
import com.ops.sc.ta.trans.datasource.ScDataSource;
import com.ops.sc.ta.trans.datasource.executor.DbExecutorFactory;

import java.sql.SQLException;

public abstract class DaoSupport {

    public static BranchDao getTransBranchDao() {
        return SingletonHolder.instance;
    }

    protected Long genID(DatabaseResource dbResource) throws SQLException {
        DatabaseType dbType = dbResource.getDataSource().getDbType();
        return DbExecutorFactory.getDbExecutor(dbType).generateID(dbResource.getOriginalConnection(), getTableName());
    }

    protected Long genID(ScDataSource scDataSource) throws SQLException {
        DatabaseType dbType = scDataSource.getDbType();
        return DbExecutorFactory.getDbExecutor(dbType).generateID(scDataSource, getTableName());
    }

    protected abstract String getTableName();

    private static class SingletonHolder {
        private static BranchDao instance = new BranchDaoImpl();
    }

}
