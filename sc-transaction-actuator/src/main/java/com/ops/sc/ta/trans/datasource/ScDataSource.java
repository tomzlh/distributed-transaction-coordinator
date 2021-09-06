package com.ops.sc.ta.trans.datasource;


import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.enums.DatabaseType;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.ta.trans.support.ScProvidedTransactionManagerRecorder;
import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.ta.trans.support.ScTransactionManager;
import com.ops.sc.ta.trans.support.ScTransactionSupport;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SmartDataSource;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public abstract class ScDataSource extends AbstractDataSource implements SmartDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScDataSource.class);

    private boolean isDefault = false;
    private ProxyFactory proxyFactory;
    private Map<String, String> primaryKeyInfo;
    private Map<String, String> balanceInfo;
    private Map<String, String> sequenceInfo;
    private String url;
    private DatabaseType dbType;

    public ScDataSource(ProxyFactory proxyFactory, DatabaseType dbType) {
        this.proxyFactory = proxyFactory;
        this.dbType = dbType;
    }

    public Map<String, String> getSequenceInfo() {
        return sequenceInfo;
    }

    public void setSequenceInfo(Map<String, String> sequenceInfo) {
        this.sequenceInfo = sequenceInfo;
    }

    public Connection getOriginalConnection() throws SQLException {
        return doGetConnection();
    }

    public Connection getOriginalConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    public DatabaseType getDbType() {
        return dbType;
    }

    public void setDbType(DatabaseType dbType) {
        this.dbType = dbType;
    }

    public String getUrl() throws ScClientException{
        if (StringUtils.isBlank(url)) {
            throw new ScClientException(ClientErrorCode.CLIENT_DATASOURCE_CONFIG_ERROR, "DataSource need url");
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getPrimaryKeyInfo() throws ScClientException{
        if (CollectionUtils.isEmpty(primaryKeyInfo)) {
            throw new ScClientException(ClientErrorCode.CLIENT_DATASOURCE_CONFIG_ERROR,
                    "DataSource need primary Key Info");
        }
        return primaryKeyInfo;
    }

    public void setPrimaryKeyInfo(Map<String, String> primaryKeyInfo) {
        this.primaryKeyInfo = primaryKeyInfo;
    }

    public Map<String, String> getBalanceInfo() throws ScClientException{
        if (CollectionUtils.isEmpty(balanceInfo)) {
            throw new ScClientException(ClientErrorCode.CLIENT_DATASOURCE_CONFIG_ERROR,
                    "DataSource need primary Key Info");
        }
        return balanceInfo;
    }

    public void setBalanceInfo(Map<String, String> balanceInfo) {
        this.balanceInfo = balanceInfo;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return internalGetConnection(null, null);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return internalGetConnection(username, password);
    }

    private Connection internalGetConnection(String username, String password) throws SQLException {
        if (TransactionContextRecorder.isFMTTransaction()) {
            if (ScProvidedTransactionManagerRecorder.isInProvidedTM()) {
                return getResourceInTA(username, password);
            } else {
                return getDatabaseResource(username, password);
            }
        } else {
            return (username == null ? doGetConnection() : doGetConnection(username, password));
        }
    }

    @Override
    public boolean shouldClose(Connection con) {
        boolean shouldClose;
        if (TransactionContextRecorder.isFMTTransaction()
                && ScProvidedTransactionManagerRecorder.isInProvidedTM()) {
            if (con instanceof DatabaseResource) {
                ScDataSource transactionalDataSource = ScTransactionManager.getTransactionDataSource();
                shouldClose = !this.equals(transactionalDataSource);
            } else {
                LOGGER.error("DataSource.shouldClose get original connection in Transaction");
                throw new ScTransactionException("Should not happen");
            }
            return shouldClose;
        }
        return doShouldClose(con);
    }


    private DatabaseResource getResourceInTA(String username, String password) throws SQLException {
        DatabaseResource resource;
        DatabaseResource transactionalResource = ScTransactionManager.getTransactionResource();
        if (transactionalResource == null) {
            resource = getDatabaseResource(username, password);
            ScTransactionManager.addToTransaction(resource);

        } else if (transactionalResource.getDataSource().equals(this)) {
            resource = transactionalResource;

        } else {
            resource = getDatabaseResource(username, password);
            try {
                ScTransactionManager.addToTransaction(resource);
            } catch (Exception e) {
                LOGGER.error("Add new scResource to transaction context fail");
                resource.close();
                throw e;
            }
        }
        return resource;
    }

    private DatabaseResource getDatabaseResource(String username, String password) throws SQLException {
        Connection conn = (username == null ? doGetConnection() : doGetConnection(username, password));
        JdbcDatabaseResource scConnection = new JdbcDatabaseResource(conn, ScTransactionSupport.getInstance(), proxyFactory,
                this);
        return scConnection;
    }

    protected abstract Connection doGetConnection() throws SQLException;

    protected abstract Connection doGetConnection(String username, String password) throws SQLException;

    protected abstract boolean doShouldClose(Connection con);
}
