package com.ops.sc.ta.trans.datasource.executor;

import com.ops.sc.common.enums.DatabaseType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DbExecutorFactory {

    private static final Map<DatabaseType, DbExecutor> DB_EXECUTOR_MAP = new ConcurrentHashMap<>();

    public static DbExecutor getDbExecutor(DatabaseType dbType) {
        return DB_EXECUTOR_MAP.get(dbType);
    }

    public static void registerDbExecutor(DatabaseType dbType, DbExecutor dbExecutor) {
        if (DB_EXECUTOR_MAP.containsKey(dbType)) {
            throw new IllegalArgumentException("DbExecutor " + dbType + " has already registered");
        }
        DB_EXECUTOR_MAP.put(dbType, dbExecutor);
    }

}
