package com.ops.sc.core.rest.config;

public class NettyConstants {

    public static String TRANSPORT_PREFIX = "netty.";

    public static String SERVICE_PREFIX = "service.";

    public static String THREAD_FACTORY_PREFIX = TRANSPORT_PREFIX + "threadFactory.";

    public static String SHUTDOWN_PREFIX = TRANSPORT_PREFIX + "shutdown.";

    /**
     * The constant BOSS_THREAD_PREFIX
     */
     public static String BOSS_THREAD_PREFIX = THREAD_FACTORY_PREFIX + "bossThreadPrefix";

    public static  String WORKER_THREAD_PREFIX = THREAD_FACTORY_PREFIX + "workerThreadPrefix";


    public static  String SHARE_BOSS_WORKER = THREAD_FACTORY_PREFIX + "shareBossWorker";


    public static  String TRANSPORT_TYPE = TRANSPORT_PREFIX + "type";

    public static  String BOSS_THREAD_SIZE = THREAD_FACTORY_PREFIX + "bossThreadSize";

    /**
     * The constant WORKER_THREAD_SIZE
     */
    public static  String WORKER_THREAD_SIZE = THREAD_FACTORY_PREFIX + "workerThreadSize";

    /**
     * The constant TRANSPORT_SERVER
     */
    public static String TRANSPORT_SERVER = TRANSPORT_PREFIX + "server";

    public static String TRANSPORT_HEARTBEAT = TRANSPORT_PREFIX + "heartbeat";

    public static String ENABLE_CLIENT_BATCH_SEND_REQUEST = TRANSPORT_PREFIX + "enableClientBatchSendRequest";

    public static String CLIENT_SELECTOR_THREAD_SIZE = THREAD_FACTORY_PREFIX + "clientSelectorThreadSize";

    public static String CLIENT_SELECTOR_THREAD_PREFIX = THREAD_FACTORY_PREFIX + "clientSelectorThreadPrefix";

    public static String CLIENT_WORKER_THREAD_PREFIX = THREAD_FACTORY_PREFIX + "clientWorkerThreadPrefix";

    public static String MIN_SERVER_POOL_SIZE = TRANSPORT_PREFIX + "minServerPoolSize";

    /**
     * The constant MAX_SERVER_POOL_SIZE.
     */
    public static String MAX_SERVER_POOL_SIZE = TRANSPORT_PREFIX + "maxServerPoolSize";

    public static String MAX_TASK_QUEUE_SIZE = TRANSPORT_PREFIX + "maxTaskQueueSize";

    /**
     * The constant KEEP_ALIVE_TIME.
     */
    public static String KEEP_ALIVE_TIME = TRANSPORT_PREFIX + "keepAliveTime";

    public static String SERVER_EXECUTOR_THREAD_PREFIX = THREAD_FACTORY_PREFIX + "serverExecutorThreadPrefix";


    public static String SHUTDOWN_WAIT = SHUTDOWN_PREFIX + "wait";

    public static String SERVER_PORT = TRANSPORT_PREFIX + "port";

    public static String SERVICE_GROUP_MAPPING_PREFIX = SERVICE_PREFIX + "vgroupMapping.";

}
