package com.ops.sc.common.constant;


/**
 * rpc 相关的参数
 */
public class RpcConstants {

    public static final String SC_LOG = "SC-GRPC-LOG";
    public static final long REQUEST_TIMEOUT_MILLS = 5000L;
    // TM二阶段上报事务结果的线程池大小，默认设置8
    public static final int DEFAULT_TSCLIENT_THREAD_POOL_SIZE = 8;
    public static final int DEFAULT_TACLIENT_THREAD_POOL_SIZE = 8;
    public static final int TS_MAX_CHANNEL_COUNT = 2;
    public static final long SHUTDOWN_TIMEOUT_MILLS = 1000;
    public static final int TA_MAX_CHANNEL_COUNT = 1;
}
