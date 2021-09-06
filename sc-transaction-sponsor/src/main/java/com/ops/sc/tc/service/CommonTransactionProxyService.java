package com.ops.sc.tc.service;

import com.google.common.collect.Maps;
import com.ops.sc.common.bean.GlobalTransRequestBean;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.rpc.dto.GlobalTransRequest;
import com.ops.sc.rpc.dto.GlobalTransResponse;
import com.ops.sc.tc.anno.DistributeTrans;
import com.ops.sc.tc.build.GlobalTransBeanBuilder;
import com.ops.sc.tc.mgr.DefaultGlobalTransProcess;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.ops.sc.common.constant.Constants.SC_GLOBAL_TRANS_PARAM;


public class CommonTransactionProxyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTransactionProxyService.class);

    private static DefaultGlobalTransProcess transactionManager = DefaultGlobalTransProcess.getInstance();

    /*public static CommonTransactionProxyService getInstance() {
        return CommonTransactionProxyService.SingletonHolder.INSTANCE;
    }*/

   public static String registerGlobalTrans(GlobalTransRequestBean globalTransRequestBean) throws ScClientException{
        GlobalTransRequest globalTransRequest = globalTransRequestToRpcRegGlobalTransRequest(globalTransRequestBean);
        saveGlobalRegisterInfoInContext(globalTransRequest);
        return registerTrans(globalTransRequest);
    }

    private static GlobalTransRequest globalTransRequestToRpcRegGlobalTransRequest(GlobalTransRequestBean globalTransRequestBean){
        return GlobalTransRequest.newBuilder().build();
    }


    public static String registerGlobalTrans(String transGroupId, String appName,
                                      DistributeTrans distributeTrans, String bizId,String dataSourceName) throws ScClientException{
        GlobalTransRequest globalTransRequest = GlobalTransBeanBuilder.buildGlobalParams(transGroupId, appName, distributeTrans, bizId,dataSourceName);
        saveGlobalRegisterInfoInContext(globalTransRequest);
        return registerTrans(globalTransRequest);
    }

    public static String registerGlobalXATrans(String transGroupId, String appName, ProceedingJoinPoint pjp,
                                           DistributeTrans xaTransaction, String bizId,String xaDataSourceName) throws ScClientException{
        GlobalTransRequest request = GlobalTransBeanBuilder.buildGlobalParams(transGroupId, appName,
                pjp, xaTransaction, bizId,xaDataSourceName);
        saveGlobalRegisterInfoInContext(request);
        return registerTrans(request);
    }



    private static void saveGlobalRegisterInfoInContext(GlobalTransRequest request) {
        Map<Object, Object> registerInfoMap = Maps.newHashMap();
        registerInfoMap.put(SC_GLOBAL_TRANS_PARAM, request);
        TransactionContextRecorder.getRegisterContext().set(registerInfoMap);
    }





    /**
     * 开启全局事务
     */
    private static String registerTrans(GlobalTransRequest request) throws ScClientException{
        GlobalTransResponse response;
        try {
            //if (RateLimiterService.acquire(request.getTransName())) {
            response = transactionManager.startGlobal(request);
            /*} else {
                String msg = request.getTransName() + " throttling";
                throw new ScClientException(ClientErrorCode.LIMITER, msg);
            }*/
        } catch (RpcException e) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, "Global transaction register failed.",
                    e);
        }
        if (!TransactionResponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, response.getBaseResponse().getMessage());
        }
        LOGGER.info("Begin new global transaction tid: {}", response.getTid());
        return response.getTid();
    }

    private static class SingletonHolder {
        private static final CommonTransactionProxyService INSTANCE = new CommonTransactionProxyService();
    }

}
