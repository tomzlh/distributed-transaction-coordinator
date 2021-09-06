package com.ops.sc.tc.service;

import com.google.common.collect.Maps;
import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.rpc.dto.GlobalTransRequest;
import com.ops.sc.rpc.dto.GlobalTransResponse;
import com.ops.sc.tc.anno.DistributeTrans;
import com.ops.sc.tc.build.GlobalTransBeanBuilder;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.common.utils.RateLimiterService;
import com.ops.sc.tc.conf.DataSourceConfiguration;
import com.ops.sc.tc.mgr.DefaultGlobalTransProcess;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

import static com.ops.sc.common.constant.Constants.SC_GLOBAL_TRANS_PARAM;

@Service("commonTransactionAspectService")
public class CommonTransactionAspectService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTransactionAspectService.class);

    private DefaultGlobalTransProcess transactionManager = DefaultGlobalTransProcess.getInstance();

    @Resource
    private DataSourceConfiguration dataSourceConfiguration;

   public Long startGlobalTrans(String transGroupId, String appName, ProceedingJoinPoint pjp,
                                DistributeTrans distributeTrans, String bizId) throws ScClientException{
        GlobalTransRequest globalTransRequest = GlobalTransBeanBuilder.buildGlobalParams(transGroupId, appName, pjp, distributeTrans, bizId,dataSourceConfiguration.getDataSourceName());
        saveGlobalRegisterInfoInContext(globalTransRequest);
        return startTrans(globalTransRequest);
    }


    public Long startGlobalTrans(String transGroupId, String appName,
                                 DistributeTrans distributeTrans, String businessId) throws ScClientException{
        String dataSourceName=distributeTrans.transMode()==TransMode.XA?dataSourceConfiguration.getXaDataSourceName():dataSourceConfiguration.getDataSourceName();
        GlobalTransRequest globalTransRequest = GlobalTransBeanBuilder.buildGlobalParams(transGroupId, appName, distributeTrans, businessId,dataSourceName);
        saveGlobalRegisterInfoInContext(globalTransRequest);
        return startTrans(globalTransRequest);
    }

    public Long startGlobalXATrans(String transGroupId, String appName, ProceedingJoinPoint pjp,
                                   DistributeTrans xaTransaction, String bizId) throws ScClientException{
        GlobalTransRequest request = GlobalTransBeanBuilder.buildGlobalParams(transGroupId, appName,
                pjp, xaTransaction, bizId,dataSourceConfiguration.getXaDataSourceName());
        saveGlobalRegisterInfoInContext(request);
        return startTrans(request);
    }


    private void saveGlobalRegisterInfoInContext(GlobalTransRequest request) {
        Map<Object, Object> registerInfoMap = Maps.newHashMap();
        registerInfoMap.put(SC_GLOBAL_TRANS_PARAM, request);
    }





    private Long startTrans(GlobalTransRequest request) throws ScClientException {
        GlobalTransResponse response;
        try {
            if (RateLimiterService.acquire(request.getBusinessId())) {
                response = transactionManager.startGlobal(request);
            } else {
                String msg = request.getBusinessId() + " throttling";
                throw new ScClientException(ClientErrorCode.RATE_LIMITER, msg);
            }
        } catch (RpcException e) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, "Global transaction register failed.",
                    e);
        }
        if (!TransactionResponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, response.getBaseResponse().getMessage());
        }
        LOGGER.info("Begin new global transaction tid: {}", response.getTid());
        return Long.parseLong(response.getTid());
    }


}
