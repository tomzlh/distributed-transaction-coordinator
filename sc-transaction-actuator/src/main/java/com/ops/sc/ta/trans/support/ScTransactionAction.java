package com.ops.sc.ta.trans.support;

import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.ta.trans.datasource.DatabaseResource;
import com.ops.sc.ta.trans.datasource.ScDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class ScTransactionAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScTransactionAction.class);
    private DatabaseResource dbResource; // 当前事务的connection
    private ScDataSource scDataSource;

    public ScTransactionAction() {
    }

    public void commit() throws SQLException {
        if (dbResource != null) {
            dbResource.commit();
        }
    }

    public void rollback() throws SQLException {
        if (dbResource != null) {
            dbResource.rollback();
        }
    }

    public void end() throws SQLException {
        if (dbResource != null) {
            DatabaseResource resource = dbResource;
            dbResource = null;
            scDataSource = null;
            resource.end();
        }
    }


    public void enlistResource(DatabaseResource dbResource) throws SQLException {
        LOGGER.debug("Add: " + dbResource.toString());
        if (this.dbResource == null) {
            this.dbResource = dbResource;
            scDataSource = dbResource.getDataSource();
            dbResource.begin();
        } else if (scDataSource.equals(dbResource.getDataSource())) {
            throw new ScTransactionException("Should not happen");
        } else {

            DatabaseResource transactionResource = this.dbResource;
            transactionResource.commit();

            scDataSource = dbResource.getDataSource();
            transactionResource.end();
            this.dbResource = null;
            dbResource.begin();
            this.dbResource = dbResource;
        }
    }

    public DatabaseResource getTransactionalResource() {
        return dbResource;
    }

    public ScDataSource getTransactionalDataSource() {
        return scDataSource;
    }
}
