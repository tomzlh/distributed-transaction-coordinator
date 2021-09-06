package com.ops.sc.common.constant;


public class ServerConstants {

    //confirm/cancel请求默认最大重试次数
    public static final Integer MAX_RETRY_TIMES = 3;

    public static final String SERVICE_NAME = "SC";


    public static class HttpAction {

        public static final String GET_TRANS_SUMMARY = "GetTransSummary";
        public static final String CREATE_TRANS_GROUP = "CreateTransGroup";
        public static final String DELETE_TRANS_GROUP = "DeleteTransGroup";
        public static final String GET_TRANS_GROUPS = "GetTransGroups";
        public static final String GET_TCC_TRANS_INFOS = "GetTccTransInfos";
        public static final String GET_FAILED_TCC_TRANS_INFOS = "GetFailedTccTransInfos";
        public static final String GET_BRANCH_TRANS_INFOS = "GetTransBranchInfos";
        public static final String RETRY_BRANCH_TRANS = "RetryBranchTrans";
        public static final String CANCEL_TIMEOUT_BRANCH_TRANS = "CancelTimeoutBranchTrans";
        public static final String CHECK_BACK_TIMEOUT_GLOBAL_TRANS = "CheckBackTimeoutGlobalTrans";
        public static final String GET_ALARM_EVENT_INFOS = "GetAlarmEventInfos";
        public static final String GET_GRPC_STREAM_MAP = "GetGrpcStreamMap";
        public static final String GET_TRANS_RECORD = "getTransRecord";
        public static final String CLEAN_TRANS_RESOURCE ="cleanTransResource";

        public static final String TCC_PREPARE_NAME ="tccPrepare";

        public static final String TCC_COMMIT_NAME ="tccCommit";

        public static final String TCC_ROLLBACK_NAME ="tccRollback";

        public static final String XA_PREPARE_NAME ="xaPrepare";

        public static final String XA_COMMIT_NAME ="xaCommit";

        public static final String XA_ROLLBACK_NAME ="xaRollback";

        public static final String SAGA_COMMIT_NAME ="sagaCommit";

        public static final String SAGA_ROLLBACK_NAME ="sagaRollback";
    }

    /**
     * HTTP常量
     */
    public static class HttpConst {
        public static final int PERMISSION_FORBIDDEN = 401;
        public static final int SC_OK = 200;

        // http请求参数
        public static final String HEADER_LANGUAGE = "sc-AcceptLanguage";
        public static final String HEADER_TENANTID = "sc-auth-tenantId";
        public static final String HEADER_ACCOUNT_ID = "sc-auth-accountId";
        public static final String HEADER_AUTH_JWT = "X-Auth-JWT";

        public static final String PARAM_ACTION = "Action";
        public static final String PARAM_VERSION = "Version";

        public static final String DEFAULT_LIMIT = "20";
        public static final String DEFAULT_OFFSET = "0";


    }


    public static final class MQConst {
        public static final Integer MQ_CONNECTION_EXPIRE = 30 * 60 * 1000; // ms
        public static final Integer MQ_CONNECTION_MAX = 200;
        public static final String KEY = "key";
    }

    /**
     * 请求参数范围限制
     */
    public static final class RequestParamsLimit {
        public static final Integer GROUP_NAME_MAX_LENGTH = 32;
    }

    /**
     * 告警模块常量
     */
    public static final class AlarmEventConst {
        public static final String ALARM_PREFIX = "alarm.";
        public static final String ALARM_CONTENT_SUFFIX = ".content";
        public static final String ALARM_DESCRIPTION_SUFFIX = ".description";
    }

    /**
     * 权限相关
     */
    public static final class AuthConst {
        public static final String RESOURCE_TYPE = "group";
        public static final String HAS_ROLE = "HasRole";
    }


    public static final class TraceConst {
        public static final String SC_SERVER = "sc-transaction-server";
        public static final String TRANS_TYPE_MSG = "transMsg";
        public static final String FMT_TYPE = "Type:FMT";
        public static final String TCC_TYPE = "Type:TCC";
        public static final String XA_TYPE = "Type:XA";
    }

}
