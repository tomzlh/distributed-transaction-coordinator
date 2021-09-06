package com.ops.sc.ta.trans.support;

import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.ta.trans.datasource.DatabaseResource;
import com.ops.sc.ta.trans.datasource.ScDataSource;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.sql.SQLException;

public class ScTransactionManager extends AbstractPlatformTransactionManager {

    public ScTransactionManager() {
        setTransactionSynchronization(SYNCHRONIZATION_NEVER);
    }

    /**
     * 获取当前事务下的databaseResource
     *
     * @return
     */
    public static DatabaseResource getTransactionResource() {
        DefaultTransactionStatus transStatus;
        try {
            transStatus = (DefaultTransactionStatus) ScTransactionAspectSupport.currentTransactionStatus();
            ScTransactionAction scTransactionAction = (ScTransactionAction) transStatus.getTransaction();
            DatabaseResource transactionResource = scTransactionAction.getTransactionalResource();
            return transactionResource;
        } catch (NoTransactionException e) {
            return null;
        }
    }

    /**
     * 获取当前事务下的DataSource
     *
     * @return
     */
    public static ScDataSource getTransactionDataSource() {
        DefaultTransactionStatus transStatus;
        try {
            transStatus = (DefaultTransactionStatus) ScTransactionAspectSupport.currentTransactionStatus();
            ScTransactionAction scTransactionAction = (ScTransactionAction) transStatus.getTransaction();
            return scTransactionAction.getTransactionalDataSource();
        } catch (NoTransactionException e) {
            return null;
        }
    }


    public static void addToTransaction(DatabaseResource dbResource) throws SQLException {
        DefaultTransactionStatus transStatus = (DefaultTransactionStatus) ScTransactionAspectSupport
                .currentTransactionStatus();
        ScTransactionAction scTransactionAction = (ScTransactionAction) transStatus.getTransaction();
        scTransactionAction.enlistResource(dbResource);
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return new ScTransactionAction();
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) {
        return false;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        // do nothing
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        ScTransactionAction scTransactionAction = (ScTransactionAction) status.getTransaction();
        try {
            scTransactionAction.commit();
        } catch (SQLException e) {
            this.setRollbackOnCommitFailure(true);
            throw new ScTransactionException("TM commit fail!", e);
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        ScTransactionAction scTransactionAction = (ScTransactionAction) status.getTransaction();
        try {
            scTransactionAction.rollback();
        } catch (SQLException e) {
            throw new ScTransactionException("TM rollback fail!", e);
        }
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        ScTransactionAction scTransactionAction = (ScTransactionAction) transaction;

        try {
            scTransactionAction.end();
        } catch (SQLException e) {
            throw new ScTransactionException("TM end fail", e);
        }
    }

}
