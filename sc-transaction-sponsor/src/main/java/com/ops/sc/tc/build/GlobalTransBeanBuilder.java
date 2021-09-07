package com.ops.sc.tc.build;

import com.google.common.base.Strings;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.ops.sc.common.bean.ModelDetail;
import com.ops.sc.common.bean.ResultCode;
import com.ops.sc.common.bean.TransactionModel;
import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.utils.InetUtil;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.rpc.dto.GlobalSagaTransRequest;
import com.ops.sc.rpc.dto.GlobalTransRollbackRequest;
import com.ops.sc.tc.anno.DistributeTrans;
import com.ops.sc.tc.model.GlobalRollbackRequest;
import com.ops.sc.tc.model.GlobalTransRequest;
import com.ops.sc.tc.model.TransQueryResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GlobalTransBeanBuilder {


    public static com.ops.sc.rpc.dto.GlobalTransRequest.Builder baseGlobalParamsBuilder(String transGroupId, String appName,
                                                                                        ProceedingJoinPoint pjp, String bizId) throws ScClientException {
        com.ops.sc.rpc.dto.GlobalTransRequest.Builder requestBuilder = com.ops.sc.rpc.dto.GlobalTransRequest.newBuilder();
        if (Strings.isNullOrEmpty(appName)) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, "AppName cannot be null");
        }
        //requestBuilder.setMode(UInt32Value.of(CommonUtils.getCurrentFrameMode().getValue()));
        requestBuilder.setAppName(appName);
        //requestBuilder.setInstanceName(getInstanceName(pjp));
        requestBuilder.setTransGroupId(transGroupId);
        requestBuilder.setCallerIp(InetUtil.getHostIp());
        requestBuilder.setBusinessId(bizId);
        return requestBuilder;
    }

    public static com.ops.sc.rpc.dto.GlobalTransRequest.Builder baseGlobalParamsBuilder(String transGroupId, String appName, String bizId) throws ScClientException{
        com.ops.sc.rpc.dto.GlobalTransRequest.Builder requestBuilder = com.ops.sc.rpc.dto.GlobalTransRequest.newBuilder();
        if (Strings.isNullOrEmpty(appName)) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, "AppName cannot be null");
        }
        //requestBuilder.setMode(UInt32Value.of(CommonUtils.getCurrentFrameMode().getValue()));
        requestBuilder.setAppName(appName);
        requestBuilder.setTransGroupId(transGroupId);
        requestBuilder.setCallerIp(InetUtil.getHostIp());
        requestBuilder.setBusinessId(bizId);
        return requestBuilder;
    }




    public  static com.ops.sc.rpc.dto.GlobalTransRequest buildGlobalParams(String transGroupId, String appName,
                                                                           ProceedingJoinPoint pjp, DistributeTrans transaction, String bizId, String dataSourceName) throws ScClientException{
        com.ops.sc.rpc.dto.GlobalTransRequest.Builder requestBuilder = baseGlobalParamsBuilder(transGroupId, appName, pjp,
                bizId);
        //requestBuilder.setTransName(transaction.name());
        requestBuilder.setTimeout(UInt64Value.of(transaction.timeout()));
        requestBuilder.setTimeoutType(UInt32Value.of(transaction.timeoutType().getValue()));
        requestBuilder.setCallbackStrategy(UInt32Value.of(transaction.callbackStrategy().getValue()));
        requestBuilder.setDataSource(dataSourceName);
        return requestBuilder.build();
    }

    public static com.ops.sc.rpc.dto.GlobalTransRequest buildGlobalParams(String transGroupId, String appName, DistributeTrans transaction, String bizId, String dataSourceName) throws ScClientException{
        com.ops.sc.rpc.dto.GlobalTransRequest.Builder requestBuilder = baseGlobalParamsBuilder(transGroupId, appName, bizId);
        requestBuilder.setTimeout(UInt64Value.of(transaction.timeout()));
        requestBuilder.setTimeoutType(UInt32Value.of(transaction.timeoutType().getValue()));
        requestBuilder.setCallbackStrategy(UInt32Value.of(transaction.callbackStrategy().getValue()));
        requestBuilder.setDataSource(dataSourceName);
        return requestBuilder.build();
    }



    public static com.ops.sc.rpc.dto.GlobalTransRequest buildGlobalTransRequest(GlobalTransRequest globalTransRequest){
        com.ops.sc.rpc.dto.GlobalTransRequest.Builder requestBuilder = com.ops.sc.rpc.dto.GlobalTransRequest.newBuilder();
        requestBuilder.setAppName(globalTransRequest.getAppName());
        requestBuilder.setTransGroupId(globalTransRequest.getTransGroupId());
        requestBuilder.setCallerIp(InetUtil.getHostIp());
        requestBuilder.setBusinessId(globalTransRequest.getBusinessId());
        requestBuilder.setTimeout(UInt64Value.of(globalTransRequest.getTimeout()));
        requestBuilder.setTimeoutType(UInt32Value.of(TimeoutType.valueOf(globalTransRequest.getTimeoutType()).getValue()));
        //requestBuilder.setCallMode(globalTransRequest.getCallMode());
        requestBuilder.setTransGroupId(globalTransRequest.getTransGroupId());
        requestBuilder.setTransMode(globalTransRequest.getTransMode());
        requestBuilder.setTransType(TransactionType.TRANSACTION.name());
        requestBuilder.setCallInParallel(UInt32Value.of(globalTransRequest.isCallInParallel()?1:0));
        List<com.ops.sc.rpc.dto.GlobalTransRequest.BranchTransDetail> branchTransDetailList=new ArrayList<>();
        if(globalTransRequest.getBranchTransRequests()!=null){
            for(GlobalTransRequest.BranchTransRequest branchTransRequest:globalTransRequest.getBranchTransRequests()) {
                branchTransDetailList.add(buildBranchTransRequest(branchTransRequest));
            }
        }
        requestBuilder.addAllBranchTransDetails(branchTransDetailList);
        return requestBuilder.build();
    }

    public static com.ops.sc.rpc.dto.GlobalTransRequest.BranchTransDetail buildBranchTransRequest(GlobalTransRequest.BranchTransRequest branchTransRequest){
        com.ops.sc.rpc.dto.GlobalTransRequest.BranchTransDetail.Builder builder=com.ops.sc.rpc.dto.GlobalTransRequest.BranchTransDetail.newBuilder();
        builder.setBranchName(branchTransRequest.getBranchName());
        builder.setBranchTransName(branchTransRequest.getBranchTransName());
        builder.setHasParent(branchTransRequest.isHasParent());
        if(branchTransRequest.getParentBranchNames()!=null) {
            builder.addAllParentBranchNames(branchTransRequest.getParentBranchNames());
        }
        builder.setRetryRequired(branchTransRequest.isRetryRequired());
        builder.setTimeout(UInt64Value.of(branchTransRequest.getTimeout()));
        builder.setTimeOutType(UInt32Value.of(TimeoutType.valueOf(branchTransRequest.getTimeoutType()).getValue()));
        if(branchTransRequest.getTransactionName()!=null) {
            builder.setTransactionName(branchTransRequest.getTransactionName());
        }
        if(branchTransRequest.getBranchTransName()!=null) {
            builder.setBranchTransName(branchTransRequest.getBranchTransName());
        }
        if(branchTransRequest.getReturnParams()!=null) {
            builder.addAllReturnParam(branchTransRequest.getReturnParams());
        }
        if(branchTransRequest.getBranchParamMap()!=null) {
            builder.putAllBranchParam(branchTransRequest.getBranchParamMap());
        }
        if(branchTransRequest.getExternalParamMap()!=null) {
            builder.putAllExternalParam(branchTransRequest.getExternalParamMap());
        }
        return builder.build();
    }


    public static GlobalTransRollbackRequest buildGlobalRollbackRequest(GlobalRollbackRequest globalRollbackRequest){
        GlobalTransRollbackRequest.Builder requestBuilder = GlobalTransRollbackRequest.newBuilder();
        requestBuilder.setIsSync(globalRollbackRequest.isSync());
        requestBuilder.setTransMode(globalRollbackRequest.getTransMode());
        requestBuilder.setTid(globalRollbackRequest.getTid());
        requestBuilder.setBusinessId(globalRollbackRequest.getBusinessId());
        return requestBuilder.build();
    }


    public static TransQueryResponse buildTransQueryRequest(com.ops.sc.rpc.dto.TransQueryResponse transQueryResponse){
        TransQueryResponse response=new TransQueryResponse();
        response.setCode(ResultCode.Success.name());
        response.setAppName(transQueryResponse.getAppName());
        response.setBusinessId(transQueryResponse.getBusinessId());
        response.setCallbackStrategy(transQueryResponse.getCallbackStrategy().getValue());
        response.setCallerIp(transQueryResponse.getCallerIp());
        response.setCallInParallel(transQueryResponse.getCallInParallel().getValue());
        response.setCallMode(transQueryResponse.getCallMode());
        response.setCreate_time(transQueryResponse.getCreateTime());
        response.setEnd_time(transQueryResponse.getEndTime());
        response.setTid(transQueryResponse.getTid());
        response.setDataSource(transQueryResponse.getDataSource());
        response.setRollbackInfo(transQueryResponse.getRollbackInfo());
        response.setStatus(transQueryResponse.getStatus());
        response.setTimeout(transQueryResponse.getTimeout().getValue());
        response.setTimeoutType(transQueryResponse.getTimeoutType().getValue());
        response.setTransMode(transQueryResponse.getTransMode());
        response.setTransGroupId(transQueryResponse.getTransGroupId());
        response.setTransType(transQueryResponse.getTransType());
        response.setBranchResponseList(new ArrayList<>());
        if(transQueryResponse.getBranchTransDetailsList()!=null){
            for(com.ops.sc.rpc.dto.TransQueryResponse.BranchTransResponse branchTransResponse:transQueryResponse.getBranchTransDetailsList()){
                TransQueryResponse.TransQueryBranchResponse transQueryBranchResponse=new TransQueryResponse.TransQueryBranchResponse();
                transQueryBranchResponse.setBid(branchTransResponse.getBid());
                transQueryBranchResponse.setBranchAppName(branchTransResponse.getBranchAppName());
                transQueryBranchResponse.setBranchName(branchTransResponse.getBranchName());
                if(branchTransResponse.getBranchParamMap()!=null) {
                    transQueryBranchResponse.setBranchParam(JsonUtil.toString(branchTransResponse.getBranchParamMap()));
                }
                if(branchTransResponse.getExternalParamMap()!=null){
                    transQueryBranchResponse.setExternalParam(JsonUtil.toString(branchTransResponse.getExternalParamMap()));
                }
                if(branchTransResponse.getReturnParamList()!=null){
                    transQueryBranchResponse.setReturnParam(JsonUtil.toString(branchTransResponse.getReturnParamList()));
                }
                if(branchTransResponse.getRollbackParamMap()!=null){
                    transQueryBranchResponse.setReturnParam(JsonUtil.toString(branchTransResponse.getRollbackParamMap()));
                }
                transQueryBranchResponse.setCommitMethod(branchTransResponse.getCommitMethod());
                transQueryBranchResponse.setRollbackMethod(branchTransResponse.getRollbackMethod());
                transQueryBranchResponse.setCreate_time(branchTransResponse.getCreateTime());
                transQueryBranchResponse.setEnd_time(branchTransResponse.getEndTime());
                transQueryBranchResponse.setHasParent(branchTransResponse.getHasParent());
                transQueryBranchResponse.setRetryRequired(branchTransResponse.getRetryRequired());
                transQueryBranchResponse.setStatus(branchTransResponse.getStatus());
                transQueryBranchResponse.setTimeout(branchTransResponse.getTimeout().getValue());
                transQueryBranchResponse.setTimeOutType(branchTransResponse.getTimeOutType().getValue());
                transQueryBranchResponse.setTransactionName(branchTransResponse.getTransactionName());
                response.getBranchResponseList().add(transQueryBranchResponse);
            }
        }
        return response;
    }

   /* public static GlobalSagaTransRequest buildGlobalSagaTransRequest(GlobalTransRequest globalTransRequest, TransactionModel transactionModel){
        GlobalSagaTransRequest.Builder requestBuilder = GlobalSagaTransRequest.newBuilder();
        requestBuilder.setAppName(globalTransRequest.getAppName());
        requestBuilder.setTransGroupId(globalTransRequest.getTransGroupId());
        requestBuilder.setCallerIp(InetUtil.getHostIp());
        requestBuilder.setBusinessId(globalTransRequest.getBusinessId());
        //requestBuilder(transactionModel.getModelName());
        requestBuilder.setTimeout(UInt64Value.of(globalTransRequest.getTimeout()));
        requestBuilder.setTimeoutType(UInt32Value.of(TimeoutType.valueOf(globalTransRequest.getTimeoutType()).getValue()));
        requestBuilder.setCallMode(globalTransRequest.getCallMode());
        //requestBuilder.setTransGroupId(globalTransRequest.getTransGroupId());
        requestBuilder.setTransMode(TransMode.SAGA.name());
        requestBuilder.setTransType(TransactionType.TRANSACTION.name());
        List<ModelDetail> modelDetailList=new ArrayList<>();
        if(transactionModel.getModelDetailList()!=null){
            Map<Long,ModelDetail> modelDetailMap=new HashMap<>();
            for(ModelDetail modelDetail:transactionModel.getModelDetailList()){
                modelDetailMap.put(modelDetail.getModelId(),modelDetail);
            }
            sortModelDetails(transactionModel.getModelDetailList(),modelDetailMap,modelDetailList);
            for(ModelDetail modelDetail:modelDetailList){
                requestBuilder.addBranchTransDetails(buildTransRequest(modelDetail));
            }
        }
        return requestBuilder.build();
    }*/

    public static GlobalSagaTransRequest buildGlobalSagaTransRequest(com.ops.sc.tc.model.GlobalSagaTransRequest globalSagaTransRequest, TransactionModel transactionModel){
        GlobalSagaTransRequest.Builder requestBuilder = GlobalSagaTransRequest.newBuilder();
        requestBuilder.setAppName(globalSagaTransRequest.getAppName());
        requestBuilder.setTransGroupId(transactionModel.getTransGroupId());
        requestBuilder.setCallerIp(InetUtil.getHostIp());
        requestBuilder.setBusinessId(globalSagaTransRequest.getBusinessId());
        //requestBuilder(transactionModel.getModelName());
        requestBuilder.setTimeout(UInt64Value.of(transactionModel.getTimeout()));
        requestBuilder.setTimeoutType(UInt32Value.of(TimeoutType.getByValue(transactionModel.getTimeoutType()).getValue()));
        requestBuilder.setCallMode(transactionModel.getCallMode());
        requestBuilder.setTransMode(TransMode.SAGA.name());
        requestBuilder.setTransType(TransactionType.TRANSACTION.name());
        List<ModelDetail> modelDetailList=new ArrayList<>();
        Map<String, com.ops.sc.tc.model.GlobalSagaTransRequest.BranchSagaTransRequest> branchSagaTransRequestMap=new HashMap<>();
        if(globalSagaTransRequest.getBranchTransRequests()!=null){
            for(com.ops.sc.tc.model.GlobalSagaTransRequest.BranchSagaTransRequest branchSagaTransRequest:globalSagaTransRequest.getBranchTransRequests()){
                branchSagaTransRequestMap.put(branchSagaTransRequest.getModelBranchName(),branchSagaTransRequest);
            }
        }
        if(transactionModel.getModelDetailList()!=null){
            Map<Long,ModelDetail> modelDetailMap=new HashMap<>();
            for(ModelDetail modelDetail:transactionModel.getModelDetailList()){
                modelDetailMap.put(modelDetail.getId(),modelDetail);
            }
            sortModelDetails(transactionModel.getModelDetailList(),modelDetailMap,modelDetailList);
            for(ModelDetail modelDetail:modelDetailList){
                requestBuilder.addBranchTransDetails(buildTransRequest(modelDetail,branchSagaTransRequestMap.get(modelDetail.getModelBranchName())));
            }
        }
        return requestBuilder.build();
    }

    private static void sortModelDetails(List<ModelDetail> modelDetailList,Map<Long,ModelDetail> modelDetailMap,List<ModelDetail> sortedList) {
        for(ModelDetail modelDetail:modelDetailList){
            addSingleModelDetail(modelDetail,modelDetailMap,sortedList);
        }
    }

    private static GlobalSagaTransRequest.BranchSagaTransDetail buildTransRequest(ModelDetail modelDetail,com.ops.sc.tc.model.GlobalSagaTransRequest.BranchSagaTransRequest branchSagaTransRequest){
        GlobalSagaTransRequest.BranchSagaTransDetail.Builder builder = GlobalSagaTransRequest.BranchSagaTransDetail.newBuilder();
        builder.setBranchTransName(modelDetail.getModelBranchName());
        builder.setBranchName(modelDetail.getBranchName());
        builder.setHasParent(modelDetail.getHasParent()==1);
        if(modelDetail.getParentNames()!=null) {
            builder.addAllParentBranchNames(modelDetail.getParentNames());
        }
        builder.setRetryRequired(modelDetail.getRetryRequired()==1);
        //builder.setRetryCount(UInt32Value.of(modelDetail.getRetryCount()));
        builder.setTimeout(UInt64Value.of(modelDetail.getTimeout()));
        builder.setTimeoutType(UInt32Value.of(TimeoutType.getByValue(modelDetail.getTimeoutType()).getValue()));
        if(!CollectionUtils.isEmpty(modelDetail.getResParamsList())) {
            builder.addAllReturnParam(modelDetail.getResParamsList());
        }
        Map<String,String> paramsMap=new HashMap<>();
        if(modelDetail.getRequestParamNames()!=null&&branchSagaTransRequest.getBranchParamMap()!=null){
            for(String paramName:modelDetail.getRequestParamNames()){
                if(branchSagaTransRequest.getBranchParamMap().get(paramName)!=null){
                    paramsMap.put(paramName,branchSagaTransRequest.getBranchParamMap().get(paramName));
                }
            }
        }
        if(!paramsMap.isEmpty()) {
            builder.putAllBranchParam(paramsMap);
        }
        Map<String,String> externalParamsMap=new HashMap<>();
        if(modelDetail.getExternalParamNames()!=null&&branchSagaTransRequest.getExternalParamMap()!=null) {
            for(String paramName:modelDetail.getExternalParamNames()){
                if(branchSagaTransRequest.getExternalParamMap().get(paramName)!=null){
                    externalParamsMap.put(paramName,branchSagaTransRequest.getExternalParamMap().get(paramName));
                }
            }
        }
        if(!externalParamsMap.isEmpty()) {
            builder.putAllExternalParam(externalParamsMap);
        }
        return builder.build();
    }

    private static void addSingleModelDetail(ModelDetail modelDetail,Map<Long,ModelDetail> modelDetailMap,List<ModelDetail> sortedList){
        if(sortedList.size()==modelDetailMap.size()){
            return;
        }
        if(modelDetail.getHasParent()!=1){
            if(!sortedList.contains(modelDetail)) {
                sortedList.add(modelDetail);
            }
        }
        else{
            for(String parentName:modelDetail.getParentNames()){
                ModelDetail parentModelDetail = modelDetailMap.get(parentName);
                if(!sortedList.contains(parentModelDetail)){
                    addSingleModelDetail(parentModelDetail,modelDetailMap,sortedList);
                }
            }
            if(!sortedList.contains(modelDetail)){
                sortedList.add(modelDetail);
            }
        }
    }



}
