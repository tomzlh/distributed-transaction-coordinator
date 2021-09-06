package com.ops.sc.ta.trans.datasource.executor.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.core.clone.context.ImageContextRecorder;
import com.ops.sc.ta.clone.dto.SqlInfo;
import com.ops.sc.ta.clone.resolver.ImageParser;
import com.ops.sc.ta.clone.resolver.ImageResolverFactory;
import com.ops.sc.ta.clone.resolver.impl.InsertImageParser;
import com.ops.sc.ta.clone.resolver.impl.SelectImageParser;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.ta.trans.datasource.executor.StatementCallback;
import com.ops.sc.ta.trans.datasource.BaseStatementProxy;
import com.ops.sc.ta.trans.datasource.DatabaseResource;
import com.ops.sc.ta.trans.datasource.ScDataSource;
import com.ops.sc.ta.trans.datasource.executor.DbExecutor;
import com.ops.sc.ta.trans.support.ScDataSourceRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;


public abstract class AbstractDbExecutor implements DbExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDbExecutor.class);

    private static final Integer MAX_SIZE = 10000;

    private static final Cache<String, List<String>> PRIMARY_KEY_FIELDS_MAP = CacheBuilder.newBuilder()
            .maximumSize(MAX_SIZE).build();

    private static final String MYSQL_SELECT_LAST_INSERT_ID = "select last_insert_id()";

    @Override
    public final <T> T execute(BaseStatementProxy stmtProxy, String sql, StatementCallback<T> callback)
            throws ScClientException,SQLException {
        // 适配Hibernate，insert之后会自动执行获取自增id
        if (MYSQL_SELECT_LAST_INSERT_ID.equals(sql)) {
            return callback.execute();
        }
        SqlInfo sqlInfo = parseSql(sql);

        if (shouldInvokeSqlWithoutSc(sqlInfo)) {
            return callback.execute();
        }

        if (stmtProxy.getDatabaseResource().getAutoCommit()) {
            return executeAutoCommitTrue(stmtProxy, sqlInfo, callback);
        } else {
            return executeAutoCommitFalse(stmtProxy, sqlInfo, callback);
        }

    }

    private <T> T executeAutoCommitTrue(BaseStatementProxy stmtProxy, SqlInfo sqlInfo, StatementCallback<T> callback)
            throws ScClientException,SQLException {
        DatabaseResource scResource = stmtProxy.getDatabaseResource();
        T result;
        try {
            scResource.getOriginalConnection().setAutoCommit(false);
            result = executeAutoCommitFalse(stmtProxy, sqlInfo, callback);
            scResource.commit();
        } catch (Throwable e) {
            scResource.rollback();
            throw new ScClientException(ClientErrorCode.COMMIT_TRANS_ERROR,e);
        } finally {
            scResource.getOriginalConnection().setAutoCommit(true);
        }
        return result;

    }

    private <T> T executeAutoCommitFalse(BaseStatementProxy stmtProxy, SqlInfo sqlInfo, StatementCallback<T> callback)
            throws ScClientException,SQLException {
        String sql = sqlInfo.getSql();
        String resourceId = getResourceId(stmtProxy.getDatabaseResource());
        ImageParser resolver = ImageResolverFactory.findResolver(sqlInfo, sql);
        // 获取table表主键field
        String pk = internalGetPrimaryKeyField(stmtProxy, resolver.getSqlInfo().getTableName());

        resolver.setSqlInfoPrimaryKey(pk);

        resolver.genBeforeImage(stmtProxy.getDatabaseResource());
        T result = callback.execute();

        // insert需要获取主键id
        if (resolver instanceof InsertImageParser) {
            getInsertPrimaryKeyValue(stmtProxy, sqlInfo);
            if (sqlInfo.getPrimaryKeyValueSet().isEmpty()) {
                throw new ScClientException(ClientErrorCode.UNSUPPORTED_DATABASE, "can not get insert pk value");
            }
        }
        // lock上下文构建
        boolean isNormalSelect = resolver instanceof SelectImageParser && !resolver.getSqlInfo().getForUpdate();
        if (isNormalSelect) {
            TransactionContextRecorder.refreshCurrentLockContext(resourceId, resolver.getSqlInfo().getTableName(),
                    resolver.getSqlInfo().getPrimaryKeyValueSet(), false);

        } else {
            TransactionContextRecorder.refreshCurrentLockContext(resourceId, resolver.getSqlInfo().getTableName(),
                    resolver.getSqlInfo().getPrimaryKeyValueSet(), true);

        }

        LOGGER.debug("Execute result: {}", result);
        resolver.genAfterImage(stmtProxy.getDatabaseResource());
        ImageContextRecorder.addRollbackItem(resolver.genRollbackItem());
        return result;
    }

    /**
     * 从缓存获取主键field，如果不命中，则调用各个db实现获取主键field的方法
     *
     * @param stmtProxy
     * @param tableName
     * @return
     */
    private String internalGetPrimaryKeyField(BaseStatementProxy stmtProxy, String tableName)  throws ScClientException{
        ScDataSource scDataSource = stmtProxy.getScDataSource();
        String beanName = ScDataSourceRecorder.getBeanNameByDataSource(scDataSource);
        String key = scDataSource.getDbType().toString() + "." + beanName + "." + tableName;

        List<String> primaryKeyList;
        try {
            primaryKeyList = PRIMARY_KEY_FIELDS_MAP.get(key,
                    () -> doGetPrimaryKeyFields(stmtProxy.getDatabaseResource(), tableName));
        } catch (ExecutionException e) {
            throw new ScClientException(ClientErrorCode.UNSUPPORTED_DATABASE,
                    "Can not get table primary key field", e);
        }

        if (CollectionUtils.isEmpty(primaryKeyList)) {
            throw new ScClientException(ClientErrorCode.UNSUPPORTED_DATABASE,
                    "Can not get table primary key field");
        }

        if (primaryKeyList.size() > 1) {
            throw new ScClientException(ClientErrorCode.UNSUPPORTED_DATABASE,
                    "sc not support multiple-column primary key table");
        }

        return primaryKeyList.get(0);

    }

    /**
     * 获取db url
     *
     * @param databaseResource
     * @return
     * @throws SQLException
     */
    private String getResourceId(DatabaseResource databaseResource) throws SQLException {
        DatabaseMetaData metaData = databaseResource.getMetaData();
        Preconditions.checkNotNull(metaData);
        String resourceId = metaData.getURL();
        LOGGER.debug("ResourceId: {}", resourceId);
        return resourceId;
    }

    /**
     * 解析sql语句
     *
     * @param sql
     * @return
     */
    protected abstract SqlInfo parseSql(String sql) throws ScClientException;

    /**
     * 获取主键字段名称
     *
     * @param tableName
     * @return
     */
    protected abstract List<String> doGetPrimaryKeyFields(DatabaseResource databaseResource, String tableName)
            throws ScClientException,SQLException;

    /**
     * 获取主键字段的值
     *
     * @param stmtProxy
     * @return
     * @throws SQLException
     */
    protected abstract void getInsertPrimaryKeyValue(BaseStatementProxy stmtProxy, SqlInfo sqlInfo) throws ScClientException,SQLException;

    protected abstract boolean shouldInvokeSqlWithoutSc(SqlInfo sqlInfo);
}
