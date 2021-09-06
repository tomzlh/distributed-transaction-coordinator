package com.ops.sc.common.enums;

/**
 * 多数据源
 *
 */
public enum DataSourceInstance {

    // 本地机房内元数据数据源
    MetaData,

    // 主机房Lock写数据源
    LockWrite,

    // 本地机房内Lock读数据源
    LockRead

}
