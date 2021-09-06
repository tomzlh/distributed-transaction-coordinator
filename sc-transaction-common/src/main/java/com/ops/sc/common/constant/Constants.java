package com.ops.sc.common.constant;

import java.io.File;
import java.nio.charset.Charset;


public class Constants {


   public static String DEFAULT_CHARSET_NAME = "UTF-8";

   public static String DEFAULT_LOAD_BALANCE = "RandomLoadBalance";

   public static String NETTY_MSG_PREFIX = "netty.";

   public static String SERIALIZE_FOR_RPC = NETTY_MSG_PREFIX + "serialization";

   public static String COMPRESSOR_FOR_RPC = NETTY_MSG_PREFIX + "compressor";


    public static final String CLIENT_PREFIX = "client.";
   /**
    * The constant LOAD_BALANCE_PREFIX.
    */
    public static final String LOAD_BALANCE_PREFIX = CLIENT_PREFIX + "loadBalance.";

    public static final String LOAD_BALANCE_TYPE = LOAD_BALANCE_PREFIX + "type";

    public static final String RANDOM_LOAD_BALANCE = "RandomLoadBalance";

    public static final String ROUND_ROBIN_LOAD_BALANCE = "RoundRobinLoadBalance";

    public static final String CONSISTENT_HASH_LOAD_BALANCE = "ConsistentHashLoadBalance";

    public static final String LEAST_ACTIVE_LOAD_BALANCE = "LeastActiveLoadBalance";

    public final static int VIRTUAL_NODES_DEFAULT = 10;




   public static String FILE_ROOT_REGISTRY = "sc";

   public static final String FILE_MIDDLE_REGISTRY = "registry";
   /**
    * The constant FILE_ROOT_CONFIG.
    */
   public static String FILE_ROOT_CONFIG = "config";

   /**
    * The constant FILE_CONFIG_SPLIT_CHAR.
    */
   public static final String FILE_CONFIG_SPLIT_CHAR = ".";
   /**
    * The constant FILE_ROOT_TYPE.
    */
   public static final String FILE_ROOT_TYPE = "type";

   public static byte[] MAGIC_CODE_BYTES = {(byte) 0xda, (byte) 0xda};


   public static byte VERSION = 1;

    /**
     * Max frame length
     */
    public static  int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    /**
     * HEAD_LENGTH of protocol v1
     */
    public static  int V1_HEAD_LENGTH = 16;



    public static Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);

    public static final Charset SC_DEFAULT_CHARSET = Charset.forName("UTF-8");

    public static final String LOCAL_MODE_PROPERTY = "sc.mode.local";

    public static final Integer MAX_RETRY_TIMES = 3;

    public static final String LOCAL_MODE_UPDATE_STATUS_KEY = "localBranchUpdate";

    public static final String DB_DEFAULT_STRING = "";

    public static final String SC_GLOBAL_TRANS_PARAM = "globalTrans";

    public static final String SC_LOGICAL_BRANCH_REGISTER_PARAM = "logicalBranchRegisterParam";

    public static final boolean MODE = false;

    public static final Long HEARTBEAT_INTERVAL = 10000L;

    public static final Integer NORMAL = 0;

    public static final Integer REGISTER = 1;

    public static final Integer HEARTBEAT_TAG = 2;

    public static final long DEFAULT_TIMEOUT = 30 * 1000L;

    public static final long CONNECTION_TIMEOUT = 5 * 1000L;
    // timeout容错量
    public static final long TIMEOUT_TOLERANT = 10 * 1000L;
    // 限流器acquire令牌最大等待时间
    public static final long RATELIMIT_TIMEOUT = 1000L;

    public static final long LOCAL_DEFAULT_TIMEOUT = 10 * 1000L;

    // 全局事务Id
    public static final String TRANS_ID = "TRANS-ID";
    // 分支事务父id
    public static final String PARENT_ID = "PARENT-ID";
    // 事务分组
    public static final String SERVER_ADDRESS = "SC-SERVER-ADDRESS";
    // 是否在全局事务中
    public static final String IN_SC_TRANSACTION = "IN-SC-TRANSACTION";
    // 事务隔离等级
    public static final String ISOLATION_LEVEL = "ISOLATION-LEVEL";

    public static final String LOGGING_PATH = "sc.logging.path";
    public static final String DEFAULT_PATH = System.getProperty("user.home") + File.separator + "sc"
            + File.separator + "logs";
    public static final String LOGGING_LEVEL = "sc.logging.level";
    public static final String DEFAULT_LEVEL = "info";
    public static final String CONFIG_PROPERTY = "sc.logging.config";
    public static final String DEFAULT_CONFIG_ENABLED = "sc.logging.default.config.enabled";

    public static final Integer PREPARE=10;

    public static final Integer COMMIT=11;

    public static final Integer CANCEL=12;


}
