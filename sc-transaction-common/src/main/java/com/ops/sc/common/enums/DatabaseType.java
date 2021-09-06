package com.ops.sc.common.enums;



public enum DatabaseType {

    MYSQL(0),

    ORACLE(1);

    private int value;

    DatabaseType(int value) {
        this.value = value;
    }

    public static DatabaseType getDefault() {
        return MYSQL;
    }

    /**
     * 是否支持通过connection获取到meta
     *
     * @param databaseType
     * @return
     */
    public static boolean isConnectionReturnMeta(DatabaseType databaseType) {
        return databaseType == MYSQL || databaseType == ORACLE;
    }

}
