package com.ops.sc.tc.api;

import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.ops.sc.common.bean.TransactionModel;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.constant.RpcConstants;
import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.common.context.TransContext;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.common.utils.RateLimiterService;
import com.ops.sc.rpc.dto.*;
import com.ops.sc.tc.grpc.sync.SponsorGrpcSyncClientBoot;
import com.ops.sc.tc.lb.ServerSelector;
import com.ops.sc.tc.service.ModelService;
import com.ops.sc.tc.service.SponsorInitService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service("transOperationApi")
public class TransOperationApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransOperationApi.class);

    @Autowired
    private ServerSelector serverSelector;

    @Autowired
    private SponsorInitService sponsorInitService;

    @Resource
    private ModelService modelService;

    @PostConstruct
    public void init(){
        sponsorInitService.init();
    }

    public Long startGlobalTrans(GlobalTransRequest request, Long timeout) throws ScClientException{
        checkGlobalTransParams(request);
        LOGGER.info("start to execute global transaction! businessId: {}", request.getBusinessId());
        GlobalTransResponse response;
        try {
            if (RateLimiterService.acquire(request.getBusinessId())) {
                try {
                    setGlobalTransContext(request.getBusinessId(),serverSelector.getServerAddress());
                    response = SponsorGrpcSyncClientBoot.getInstance().getTSClient(TransactionContextRecorder.getServerAddress())
                            .startGlobalTransSync(request, timeout==null?RpcConstants.REQUEST_TIMEOUT_MILLS:timeout);
                } finally {
                    TransactionContextRecorder.clearAllTransContext();
                }
            } else {
                String msg = request.getBusinessId() + " throttling";
                throw new ScClientException(ClientErrorCode.RATE_LIMITER, msg);
            }
        } catch (Exception e) {
            LOGGER.error("execute global transaction error: businessId:{}, appName:{}",request.getBusinessId(),request.getAppName(),e);
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, "start global transaction failed. businessId:"+request.getBusinessId(),
                    e);
        }
        if (!TransactionResponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
            LOGGER.error("execute global transaction failed: businessId:{}, Tid:{}",request.getBusinessId(),response.getTid());
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, response.getBaseResponse().getMessage());
        }
        LOGGER.info("execute global transaction succeed! tid: {}, businessId: {}", response.getTid(),response.getBaseResponse().getBusinessId());
        return Long.parseLong(response.getTid());
    }


    public Long startGlobalSagaTrans(String transCode,GlobalSagaTransRequest request,Long timeout) throws ScClientException {
        checkGlobalTransParams(request);
        GlobalSagaTransResponse response=null;
        TransactionModel transactionModel=modelService.getModel(transCode);
        if(transactionModel==null){
            LOGGER.error("no model found, transCode:{},businessId:{}",transCode,request.getBusinessId());
            throw new ScClientException(ClientErrorCode.MODEL_GET_ERROR,"get model error,transCode:"+transCode+" businessId:"+request.getBusinessId());
        }
        try {
            if (RateLimiterService.acquire(request.getBusinessId())) {
                if(TransactionType.valueOf(request.getTransType()) == TransactionType.TRANSACTION) {
                    try {
                        setGlobalTransContext(request.getBusinessId(), serverSelector.getServerAddress());
                        response = SponsorGrpcSyncClientBoot.getInstance().getTSClient(TransactionContextRecorder.getServerAddress())
                                .startGlobalSagaTransSync(request, timeout == null ? RpcConstants.REQUEST_TIMEOUT_MILLS : timeout);
                    } finally {
                        TransactionContextRecorder.clearAllTransContext();
                    }
                } else {
                    String msg = request.getBusinessId() + " throttling";
                    throw new ScClientException(ClientErrorCode.RATE_LIMITER, msg);
                }
            }
        } catch (Exception e) {
            LOGGER.error("execute global saga transaction error: businessId:{}, appName:{}",request.getBusinessId(),request.getAppName(),e);
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, "start global saga transaction failed. businessId:"+request.getBusinessId(),
                    e);
        }
        return Long.parseLong(response.getTid());
    }



    public TransQueryResponse queryGlobalTrans(final TransQueryRequest transQueryRequest,Long timeout) throws ScClientException{
        try {
            return SponsorGrpcSyncClientBoot.getInstance().getTSClient(serverSelector.getServerAddress()).findGlobalTransSync(transQueryRequest, timeout == null ? RpcConstants.REQUEST_TIMEOUT_MILLS : timeout);
        }catch (Exception e){
            LOGGER.error("query global transaction error: businessId:{}",transQueryRequest.getBusinessId());
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED,"start global transaction error: businessId:"+transQueryRequest.getBusinessId());
        }
    }

    public boolean rollbackGlobalTrans(GlobalTransRollbackRequest request, Long timeout) throws ScClientException {
        try {
            if (RateLimiterService.acquire(request.getBusinessId())) {
                GlobalTransRollbackResponse response = SponsorGrpcSyncClientBoot.getInstance().getTSClient(TransactionContextRecorder.getServerAddress())
                        .rollbackGlobalTransSync(request, timeout == null ? RpcConstants.REQUEST_TIMEOUT_MILLS : timeout);
                return TransactionResponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed();
            } else {
                String msg = request.getBusinessId() + " throttling";
                throw new ScClientException(ClientErrorCode.RATE_LIMITER, msg);
            }
        }catch (Exception e){
            LOGGER.error("rollback global saga transaction error: businessId:{}",request.getBusinessId());
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED,"rollback global transaction error: businessId:"+request.getBusinessId());
        }
    }


    private GlobalTransRequest checkGlobalTransParams(final GlobalTransRequest request)  {
        GlobalTransRequest.Builder builder = request.toBuilder();
        if (StringUtils.isBlank(builder.getAppName())) {
            throw new ScTransactionException(TransactionResponseCode.PARAM_IS_REQUIRED, "appName");
        }
        if (StringUtils.isBlank(builder.getTransGroupId())) {
            throw new ScTransactionException(TransactionResponseCode.PARAM_IS_REQUIRED, "groupId");
        }
        if (StringUtils.isBlank(builder.getCallerIp())) {
            throw new ScTransactionException(TransactionResponseCode.PARAM_IS_REQUIRED, "callerIp");
        }
        if (builder.getBranchTransDetailsBuilderList()==null||builder.getBranchTransDetailsBuilderList().isEmpty()) {
            throw new ScTransactionException(TransactionResponseCode.PARAM_IS_REQUIRED, "branchTransDetails");
        }
        if (!builder.hasTimeout()) {
            builder.setTimeout(UInt64Value.of(Constants.DEFAULT_TIMEOUT));
        } else if (builder.getTimeout().getValue() <= 0) {
            throw new ScTransactionException(TransactionResponseCode.PARAM_INVALID, "timeout");
        }
        if (!builder.hasCallbackStrategy()) {
            builder.setCallbackStrategy(UInt32Value.of(CallbackStrategy.getDefault().getValue()));
        } else {
            CallbackStrategy.getCallbackStrategyByValue(builder.getCallbackStrategy().getValue());
        }

        if (!builder.hasTimeoutType()) {
            builder.setTimeoutType(UInt32Value.of(TimeoutType.getDefault().getValue()));
        } else {
            TimeoutType.getByValue(builder.getTimeoutType().getValue());
        }
        return builder.build();
    }


    private GlobalSagaTransRequest checkGlobalTransParams(final GlobalSagaTransRequest request)  {
        GlobalSagaTransRequest.Builder builder = request.toBuilder();
        if (StringUtils.isBlank(builder.getAppName())) {
            throw new ScTransactionException(TransactionResponseCode.PARAM_IS_REQUIRED, "appName");
        }
        if (StringUtils.isBlank(builder.getTransGroupId())) {
            throw new ScTransactionException(TransactionResponseCode.PARAM_IS_REQUIRED, "groupId");
        }
        if (StringUtils.isBlank(builder.getCallerIp())) {
            throw new ScTransactionException(TransactionResponseCode.PARAM_IS_REQUIRED, "callerIp");
        }
        if (builder.getBranchTransDetailsBuilderList()==null||builder.getBranchTransDetailsBuilderList().isEmpty()) {
            throw new ScTransactionException(TransactionResponseCode.PARAM_IS_REQUIRED, "branchTransDetails");
        }
        if (!builder.hasTimeout()) {
            builder.setTimeout(UInt64Value.of(Constants.DEFAULT_TIMEOUT));
        } else if (builder.getTimeout().getValue() <= 0) {
            throw new ScTransactionException(TransactionResponseCode.PARAM_INVALID, "timeout");
        }

        if (!builder.hasTimeoutType()) {
            builder.setTimeoutType(UInt32Value.of(TimeoutType.getDefault().getValue()));
        } else {
            TimeoutType.getByValue(builder.getTimeoutType().getValue());
        }
        return builder.build();
    }


    private void setGlobalTransContext(String businessId,String serverAddress) {
        TransContext transContext=new TransContext(businessId,serverAddress);
        TransactionContextRecorder.setCurrentTransContext(transContext);
    }



}
