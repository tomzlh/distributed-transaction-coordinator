package com.ops.sc.core.build;


import com.google.protobuf.UInt32Value;
import com.ops.sc.common.bean.ScRequestMessage;
import com.ops.sc.common.bean.ScSagaRequestMessage;
import com.ops.sc.common.enums.MessageType;
import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.model.TransBranchInfo;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.utils.InetUtil;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.rpc.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RpcRequestBuilder {

    public static TransQueryRequest buildQueryGlobalRequest(String requestId, String transLevel) {
        TransQueryRequest.Builder builder = TransQueryRequest.newBuilder();
        builder.setBusinessId(requestId);
        //builder.setTransactionLevel(transLevel);
        return builder.build();
    }

    public static BranchTransQueryRequest buildBranchInfoQueryRequest(String requestId, String tid) {
        BranchTransQueryRequest.Builder builder = BranchTransQueryRequest.newBuilder();
        builder.setRequestId(requestId);
        builder.setTid(tid);
        return builder.build();
    }


    /*public static BranchPrepareRequest buildBranchPrepareRequest(TransactionInfo transactionInfo,TransBranchInfo transBranchInfo){
        BranchPrepareRequest branchPrepareRequest = new BranchPrepareRequest();
        branchPrepareRequest.setBusinessId(transactionInfo.getBusinessId());
        branchPrepareRequest.setTid(transactionInfo.getTid());
        branchPrepareRequest.setBranchId(transBranchInfo.getBid());
        branchPrepareRequest.setDataSource(transBranchInfo.getDataSource());
        branchPrepareRequest.setTransactionName(transBranchInfo.getTransactionName());
        branchPrepareRequest.setBranchName(transactionInfo.getAppName());
        branchPrepareRequest.setApplicationData(JsonUtil.toString(transBranchInfo.getParamMap()));
        branchPrepareRequest.setTransMode(transactionInfo.getTransMode().name());
        return branchPrepareRequest;
    }*/


    /*public static BranchCommitRequest buildBranchCommitRequest(TransactionInfo transactionInfo,TransBranchInfo transBranchInfo){
        BranchCommitRequest branchCommitRequest= new BranchCommitRequest();
        branchCommitRequest.setBusinessId(transactionInfo.getBusinessId());
        branchCommitRequest.setTid(transactionInfo.getTid());
        branchCommitRequest.setBranchId(transBranchInfo.getBid());
        branchCommitRequest.setDataSource(transBranchInfo.getDataSource());
        branchCommitRequest.setBranchName(transactionInfo.getAppName());
        branchCommitRequest.setTransactionName(transBranchInfo.getTransactionName());
        branchCommitRequest.setApplicationData(JsonUtil.toString(transBranchInfo.getParamMap()));
        branchCommitRequest.setTransMode(transactionInfo.getTransMode().name());
        return branchCommitRequest;
    }*/

    /*public static BranchCommitRequest buildBranchCommitRequest(String businessId,TransMode transMode,TransBranchInfo transBranchInfo){
        BranchCommitRequest branchCommitRequest= new BranchCommitRequest();
        branchCommitRequest.setBusinessId(businessId);
        branchCommitRequest.setTid(transBranchInfo.getTid());
        branchCommitRequest.setBranchId(transBranchInfo.getBid());
        branchCommitRequest.setDataSource(transBranchInfo.getDataSource());
        branchCommitRequest.setBranchName(transBranchInfo.getAppName());
        branchCommitRequest.setTransactionName(transBranchInfo.getTransactionName());
        branchCommitRequest.setApplicationData(JsonUtil.toString(transBranchInfo.getParamMap()));
        branchCommitRequest.setTransMode(transMode.name());
        return branchCommitRequest;
    }*/

    public static ScRequestMessage buildBranchRequest(TransBranchInfo transBranchInfo, MessageType messageType){
        ScRequestMessage scRequestMessage = new ScRequestMessage();
        scRequestMessage.setBusinessId(transBranchInfo.getBusinessId());
        scRequestMessage.setTid(transBranchInfo.getTid());
        scRequestMessage.setBranchId(transBranchInfo.getBid());
        scRequestMessage.setDataSource(transBranchInfo.getDataSource());
        scRequestMessage.setBranchName(transBranchInfo.getBranchTransName());
        scRequestMessage.setTransactionName(transBranchInfo.getTransactionName());
        scRequestMessage.setReturnParams(transBranchInfo.getReturnParamList());
        scRequestMessage.setParamMap(transBranchInfo.getParamMap());
        if(transBranchInfo.getExternalMap()!=null){
            if(scRequestMessage.getParamMap()!=null){
                scRequestMessage.setParamMap(new HashMap<>());
            }
            scRequestMessage.getParamMap().putAll(transBranchInfo.getExternalMap());
        }
        scRequestMessage.setTransMode(transBranchInfo.getTransMode());
        scRequestMessage.setMessageType(messageType.getValue());
        scRequestMessage.setReturnParams(transBranchInfo.getReturnParamList());
        //branchRollbackRequest.setApplicationData(JsonUtil.toString(transBranchInfo.getParamMap()));
        scRequestMessage.setTransMode(transBranchInfo.getTransMode());
        return scRequestMessage;
    }


    public static ScSagaRequestMessage buildSagaBranchRequest(TransBranchInfo transBranchInfo, MessageType messageType){
        ScSagaRequestMessage scRequestMessage = new ScSagaRequestMessage();
        scRequestMessage.setBusinessId(transBranchInfo.getBusinessId());
        scRequestMessage.setTid(transBranchInfo.getTid());
        scRequestMessage.setBid(transBranchInfo.getBid());
        scRequestMessage.setBranchName(transBranchInfo.getBranchTransName());
        scRequestMessage.setTransactionName(transBranchInfo.getTransactionName());
        scRequestMessage.setParamMap(transBranchInfo.getParamMap());
        scRequestMessage.setTransMode(transBranchInfo.getTransMode());
        scRequestMessage.setMessageType(messageType.getValue());
        scRequestMessage.setReturnParams(transBranchInfo.getReturnParamList());
        //branchRollbackRequest.setApplicationData(JsonUtil.toString(transBranchInfo.getParamMap()));
        return scRequestMessage;
    }

    public static ScSagaRequestMessage buildSagaBranchRequest(ScBranchRecord scBranchRecord, MessageType messageType){
        ScSagaRequestMessage scRequestMessage = new ScSagaRequestMessage();
        scRequestMessage.setBusinessId(scBranchRecord.getBusinessId());
        scRequestMessage.setTid(scBranchRecord.getTid());
        scRequestMessage.setBid(scBranchRecord.getBid());
        scRequestMessage.setBranchName(scBranchRecord.getBranchName());
        scRequestMessage.setTransactionName(scBranchRecord.getTransactionName());
        if(scBranchRecord.getBranchParam()!=null) {
            scRequestMessage.setParamMap(JsonUtil.toMap(scBranchRecord.getBranchParam()));
        }
        scRequestMessage.setTransMode(TransMode.fromId(scBranchRecord.getTransMode()).name());
        scRequestMessage.setMessageType(messageType.getValue());
        if(scBranchRecord.getReturnParam()!=null) {
            scRequestMessage.setReturnParams(JsonUtil.toObject(List.class,scBranchRecord.getReturnParam()));
        }
        return scRequestMessage;
    }


    public static Map<String,Object> buildSagaBranchMapRequest(TransBranchInfo transBranchInfo, MessageType messageType){
        Map<String,Object> map=new HashMap<>();
        map.put("businessId",transBranchInfo.getBusinessId());
        map.put("tid",transBranchInfo.getTid());
        map.put("bid",transBranchInfo.getBid());
        map.put("requestParams",transBranchInfo.getParamMap());
        map.put("responseParams",transBranchInfo.getReturnParamList());
        map.put("messageType",messageType.getValue());
        return map;
    }

    public static Map<String,Object> buildSagaBranchMapRequest(ScBranchRecord scBranchRecord, MessageType messageType){
        Map<String,Object> map=new HashMap<>();
        map.put("businessId",scBranchRecord.getBusinessId());
        map.put("tid",scBranchRecord.getTid());
        map.put("bid",scBranchRecord.getBid());
        map.put("requestParams",scBranchRecord.getBranchParam());
        map.put("responseParams",scBranchRecord.getReturnParam());
        map.put("messageType",messageType.getValue());
        return map;
    }





    /*public static BranchRollbackRequest buildBranchRollbackRequest(String businessId,TransMode transMode,TransBranchInfo transBranchInfo){
        BranchRollbackRequest branchRollbackRequest= new BranchRollbackRequest();
        branchRollbackRequest.setBusinessId(businessId);
        branchRollbackRequest.setTid(transBranchInfo.getTid());
        branchRollbackRequest.setBranchId(transBranchInfo.getBid());
        branchRollbackRequest.setDataSource(transBranchInfo.getDataSource());
        branchRollbackRequest.setBranchName(transBranchInfo.getAppName());
        branchRollbackRequest.setTransactionName(transBranchInfo.getTransactionName());
        branchRollbackRequest.setApplicationData(JsonUtil.toString(transBranchInfo.getParamMap()));
        branchRollbackRequest.setTransMode(transMode.name());
        return branchRollbackRequest;
    }*/

    public static BranchTransRequest buildBranchTransRequest(String businessId,int operateType,ScBranchRecord scBranchRecord){
        return BranchTransRequest.newBuilder().setBusinessId(businessId).setTid(String.valueOf(scBranchRecord.getTid())).setBid(String.valueOf(scBranchRecord.getBid()))
                .setBranchTransName(scBranchRecord.getBranchTransName()).setOperateType(UInt32Value.of(operateType)).setBranchType(UInt32Value.of(scBranchRecord.getTransMode())).build();
    }

    public static TransCompensationRequest buildTransCompensationRequest(ScTransRecord scTransRecord){
        return TransCompensationRequest.newBuilder().setBusinessId(scTransRecord.getBusinessId()).setTid(String.valueOf(scTransRecord.getTid())).setAppName(scTransRecord.getAppName())
                .setCallerIp(InetUtil.getHostIp()).build();
    }

}
