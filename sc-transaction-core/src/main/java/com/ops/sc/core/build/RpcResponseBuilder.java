package com.ops.sc.core.build;

import java.util.List;
import java.util.Locale;

import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.ops.sc.common.enums.*;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.core.service.ResourceInfoService;
import com.ops.sc.core.util.ApplicationUtils;
import com.ops.sc.rpc.dto.ParentResponse;
import com.ops.sc.rpc.dto.TransQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;


public class RpcResponseBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcResponseBuilder.class);

    private static ResourceInfoService resourceInfoService = ApplicationUtils
            .getBean(ResourceInfoService.class);

    public static ParentResponse buildErrorBaseResponse(TransactionResponseCode transactionResponseCode, String businessId, String errorArgs) {
        ParentResponse.Builder builder = ParentResponse.newBuilder();

        try {
            String message = resourceInfoService.getMessage(transactionResponseCode.getDescKey(), Locale.ENGLISH,
            Locale.ENGLISH, errorArgs);
            builder.setMessage(message);
        } catch (NoSuchMessageException e) {
            LOGGER.error("messageSource get message catch an NoSuchMessageException: ", e);
        }
        builder.setCode(transactionResponseCode.getCode());
        return builder.build();
    }

    public static ParentResponse buildSuccessBaseResponse(String businessId) {
        ParentResponse.Builder builder = ParentResponse.newBuilder();
        try {
            String message = resourceInfoService.getMessage(TransactionResponseCode.SUCCESS.getDescKey(), Locale.ENGLISH,
                    Locale.ENGLISH, null);
            builder.setMessage(message);
            builder.setBusinessId(businessId);
        } catch (NoSuchMessageException e) {
            LOGGER.error("messageSource get message catch an NoSuchMessageException: ", e);
        }
        builder.setCode(TransactionResponseCode.SUCCESS.getCode());
        return builder.build();
    }


    public static TransQueryResponse.Builder buildTransQueryResponse(ScTransRecord scTransRecord){
        TransQueryResponse.Builder builder=TransQueryResponse.newBuilder();
        builder.setTid(String.valueOf(scTransRecord.getTid()));
        builder.setTransGroupId(scTransRecord.getGroupId());
        builder.setBusinessId(scTransRecord.getBusinessId());
        TransMode transMode= TransMode.valueOf(scTransRecord.getTransMode());
        builder.setTransMode(transMode.name());
        builder.setStatus(TransStatus.getTransStatusByValue(scTransRecord.getStatus()).name());
        builder.setCallerIp(scTransRecord.getCallerIp());
        builder.setTimeout(UInt64Value.of(scTransRecord.getTimeout()==null?0:scTransRecord.getTimeout()));
        builder.setTimeoutType(UInt32Value.of(scTransRecord.getTimeoutType()));
        builder.setAppName(scTransRecord.getAppName());
        builder.setCallbackStrategy(UInt32Value.of(scTransRecord.getCallbackStrategy()));
        builder.setDataSource(scTransRecord.getDataSource());
        builder.setTransType(scTransRecord.getEventType()==null?"":scTransRecord.getEventType());
        CallMode callMode = CallMode.getCallModeByValue(scTransRecord.getCallInParallel());
        if(callMode!=null) {
            builder.setCallMode(callMode.name());
        }
        builder.setCallerIp(scTransRecord.getCallerIp());
        for(ScBranchRecord branchTransaction:scTransRecord.getBranchTransactionList()){
            builder.addBranchTransDetails(scBranchTransactionToBranchReponse(branchTransaction));
        }
        builder.setRollbackInfo(scTransRecord.getRollBackInfo());
        return builder;
    }

    public static TransQueryResponse.BranchTransResponse scBranchTransactionToBranchReponse(ScBranchRecord scBranchRecord){
        TransQueryResponse.BranchTransResponse.Builder builder=TransQueryResponse.BranchTransResponse.newBuilder();
        builder.setBranchName(scBranchRecord.getBranchName());
        builder.setBranchAppName(scBranchRecord.getBranchTransName());
        builder.setTransactionName(scBranchRecord.getTransactionName());
        builder.setUrl(scBranchRecord.getUrl());
        if(scBranchRecord.getBranchParam()!=null) {
            builder.putAllBranchParam(JsonUtil.toMap(scBranchRecord.getBranchParam()));
        }
        if(scBranchRecord.getExternalParam()!=null) {
            builder.putAllExternalParam(JsonUtil.toMap(scBranchRecord.getExternalParam()));
        }
        if(scBranchRecord.getRollbackParam()!=null){
            builder.putAllRollbackParam(JsonUtil.toMap(scBranchRecord.getRollbackParam()));
        }
        if(scBranchRecord.getReturnParam()!=null){
            builder.addAllReturnParam(JsonUtil.toObject(List.class,scBranchRecord.getReturnParam()));
        }
        builder.setHasParent(scBranchRecord.getHasParent()==1?true:false);
        if(scBranchRecord.getParentName()!=null) {
            builder.addParentBranchNames(scBranchRecord.getParentName());
        }
        builder.setRetryRequired(scBranchRecord.getRetry()==1?true:false);
        builder.setTimeout(UInt64Value.of(scBranchRecord.getTimeout()==null?0:scBranchRecord.getTimeout()));
        builder.setTimeOutType(UInt32Value.of(scBranchRecord.getTimeoutType()));
        builder.setCreateTime(scBranchRecord.getCreateTime().toString());
        if(scBranchRecord.getEndTime()!=null) {
            builder.setEndTime(scBranchRecord.getEndTime().toString());
        }
        return builder.build();
    }

}
