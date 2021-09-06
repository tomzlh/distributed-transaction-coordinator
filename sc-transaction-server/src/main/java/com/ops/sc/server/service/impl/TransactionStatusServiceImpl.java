package com.ops.sc.server.service.impl;

import com.ops.sc.common.bean.*;
import com.ops.sc.common.constant.ServerConstants;
import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.ScServerException;
import com.google.common.base.Strings;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.TimeoutType;
import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.thread.NamedThreadFactory;
import com.ops.sc.common.utils.DistributeIdGenerator;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.core.build.RpcRequestBuilder;
import com.ops.sc.core.glock.LockManager;
import com.ops.sc.rpc.dto.*;
import com.ops.sc.core.gather.TransInfoBuilder;
import com.ops.sc.server.cache.TransGroupCache;
import com.ops.sc.server.recorder.TaServiceRecorder;
import com.ops.sc.server.service.GlobalTransService;
import com.ops.sc.server.service.*;
import com.ops.sc.core.build.RpcResponseBuilder;
import com.ops.sc.common.model.TransBranchInfo;
import com.ops.sc.common.model.TransactionInfo;
import com.ops.sc.server.transaction.TransactionProcessorFactory;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

import static com.ops.sc.common.enums.TransProcessMode.FMT;
import static com.ops.sc.common.enums.TransProcessMode.LOGIC_BRANCH;
import static com.ops.sc.core.build.RpcResponseBuilder.buildSuccessBaseResponse;


@Service
public class TransactionStatusServiceImpl implements TransactionStatusService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionStatusServiceImpl.class);

    @Resource
    private LockManager lockManager;

    @Resource
    private MetaService metaService;


    @Resource
    private TransExecuteService transExecuteService;

    @Resource
    private AlarmService alarmService;

    @Autowired
    private TransGroupCache transGroupCache;

    @Resource
    private GlobalTransService globalTransService;

    @Resource
    private BranchTransService branchTransService;

    @Resource
    private CallAction callAction;

    @Autowired
    private CallService callService;
    @Autowired
    private TransactionProcessorFactory transactionProcessorFactory;

    private ExecutorService compensatorExecutor= new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(),
            2000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new NamedThreadFactory("_COMPENSATOR_EXECUTOR", 3, true));

    private ThreadPoolExecutor transactionExecutor =new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors()*2,
                           2000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new NamedThreadFactory("_TRANS_EXECUTOR", 3, true));

    @Override
    @SuppressWarnings("unchecked")
    public BranchTransResponse processBranchTrans(final BranchTransRequest request) {
        BranchTransResponse branchTransResponse=null;

        if(request.getOperateType().getValue()==Constants.PREPARE){
            branchTransResponse=branchPrepare(request);
        }
        else if(request.getOperateType().getValue()==Constants.COMMIT){
            branchTransResponse=branchCommit(request);
        }
        else if(request.getOperateType().getValue()==Constants.CANCEL){
            branchTransResponse=branchRollback(request);
        }
        return branchTransResponse;
    }

    private BranchTransResponse branchRegister(final BranchTransRequest request){
        LOGGER.info("RegisterBranchTrans tid : {}", request.getTid());
        LOGGER.debug("Request : {}", request.toString());
        checkRequestParams(request);
        BranchTransRequest.Builder builder = request.toBuilder();
       /* if (!request.hasTimeout()) {
            builder.setTimeout(UInt64Value.of(Constants.DEFAULT_TIMEOUT));
        } else if (request.getTimeout().getValue() < 0) {
            throw new ScServerException(TransactionReponseCode.PARAM_INVALID, "tryTimeout");
        }

        if (!request.hasTimeoutStrategy()) {
            builder.setTimeoutStrategy(UInt32Value.of(TimeoutType.getDefault().getValue()));
        } else {
            TimeoutType.getByValue(request.getTimeoutStrategy().getValue());
        }

        if (!request.hasStatus()) {
            builder.setStatus(UInt32Value.of(TransStatus.TRYING.getValue()));
        } else if (!TransStatus.TRYING.getValue().equals(request.getStatus().getValue())
                && !TransStatus.TRY_SUCCEED.getValue().equals(request.getStatus().getValue())) {
            throw new ScServerException(TransactionReponseCode.PARAM_INVALID, "status");
        }*/

        ScTransRecord scTransRecord = globalTransService.getByTid(Long.parseLong(request.getTid()));
        if (scTransRecord == null) {
            throw new ScServerException(TransactionResponseCode.TRANS_NOT_EXIST,"transaction is not exist!");
        }

        if (!scTransRecord.getStatus().equals(TransStatus.TRYING.getValue())) {
            LOGGER.warn("RegisterBranchTrans fail. Status is not in trying, status : {}, tid : {}", scTransRecord.getStatus(),
                    scTransRecord.getTid());
            throw new ScServerException(TransactionResponseCode.GLOBAL_TRANS_OPERATE_ILLEGAL,"transaction status is illegal!");
        }

        Long branchId;
        if (request.getBranchType().getValue() == LOGIC_BRANCH.getValue()
                && !Strings.isNullOrEmpty(request.getBid())) {
            branchId = Long.parseLong(request.getBid());
        } else {
            branchId = DistributeIdGenerator.generateId();
        }

        BranchTransResponse.Builder responseBuilder = BranchTransResponse.newBuilder();

        // FMT模式需要加锁
        if (request.getBranchType().getValue() == FMT.getValue() && !lockManager.acquireLock(request, branchId)) {
            responseBuilder.setBaseResponse(RpcResponseBuilder.buildErrorBaseResponse(TransactionResponseCode.LOCK_CONFLICT,request.getBusinessId(),"FMT requires lock!"));
            return responseBuilder.build();
        }

        ScBranchRecord scBranchRecord = TransInfoBuilder.getScBranchInfo(Long.parseLong(request.getTid()),branchId, builder.build());
        branchTransService.save(scBranchRecord);

        LOGGER.info("RegisterBranchTrans success , tid : {},branchId : {}", scBranchRecord.getTid(), branchId);

        responseBuilder.setBranchId(String.valueOf(branchId));
        responseBuilder.setBaseResponse(buildSuccessBaseResponse(request.getBusinessId()));
        return responseBuilder.build();
    }


    public TransCompensationResponse transactionCompensate(final TransCompensationRequest request){
        LOGGER.info("Prepare BranchTrans tid : {}", request.getTid());
        LOGGER.debug("Prepare Request : {}", request.toString());
        try {
            checkRequestParams(request);
            ScTransRecord scTransRecord = globalTransService.getByTid(Long.parseLong(request.getTid()));
            if(scTransRecord!=null){
                List<ScBranchRecord> branchRecordList = branchTransService.getTransBranchInfoList(scTransRecord.getTid());
                if(CollectionUtils.isEmpty(branchRecordList)){
                    ParentResponse parentResponse=ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setMessage("no branch info!").setCode(ResultCode.Failed.name()).build();
                    return TransCompensationResponse.newBuilder().setTid(request.getTid()).setBusinessId(request.getBusinessId()).setBaseResponse(parentResponse).build();
                }
                scTransRecord.setBranchTransactionList(branchRecordList);
                setUrls(scTransRecord.getBranchTransactionList());
                if(TransMode.valueOf(scTransRecord.getTransMode())==TransMode.SAGA){
                    processSagaTransaction(scTransRecord,TransStatus.getTransStatusByValue(scTransRecord.getStatus()));
                }
                else{
                    processTccAndXaTransaction(scTransRecord,TransStatus.getTransStatusByValue(scTransRecord.getStatus()));
                }
            }
        }catch (Exception e){
            LOGGER.debug("Execute Prepare Request failed: {}", request);
            ParentResponse parentResponse=ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Failed.name()).build();
            return TransCompensationResponse.newBuilder().setTid(request.getTid()).setBusinessId(request.getBusinessId()).setBaseResponse(parentResponse).build();
        }
        ParentResponse parentResponse=ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Success.name()).build();
        return TransCompensationResponse.newBuilder().setTid(request.getTid()).setBusinessId(request.getBusinessId()).setBaseResponse(parentResponse).build();
    }


    private void processTccAndXaTransaction(ScTransRecord scTransRecord, TransStatus globalTransStatus) {
        if (globalTransStatus == TransStatus.READY||globalTransStatus == TransStatus.TRYING) {
            boolean isAllPrepareSucceed=true;
            List<ScBranchRecord> scBranchRecordList=new ArrayList<>();
            for(ScBranchRecord scBranchRecord: scTransRecord.getBranchTransactionList()) {
                if(!scBranchRecordList.contains(scBranchRecord)){
                    scBranchRecordList.add(scBranchRecord);
                }
                try {
                    ScResponseMessage scResponseMessage = null;
                    if(TransMode.TCC==TransMode.fromId(scBranchRecord.getTransMode())) {
                         scResponseMessage = processCall(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.TCC_PREPARE_NAME),scBranchRecord, MessageType.TYPE_BRANCH_PREPARE);
                    }
                    else if(TransMode.XA==TransMode.fromId(scBranchRecord.getTransMode())){
                        scResponseMessage = processCall(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.XA_PREPARE_NAME),scBranchRecord, MessageType.TYPE_BRANCH_PREPARE);
                    }
                    if (!TransactionResponseCode.SUCCESS.getCode().equals(scResponseMessage.getResultInfo().code)) {
                        isAllPrepareSucceed = false;
                        updateBranchRecord(scBranchRecord.getBid(),TransStatus.TRY_FAILED.getValue(),scBranchRecord.getRetryCount()+1);
                        break;
                    }
                    else{
                        updateBranchRecord(scBranchRecord.getBid(),TransStatus.TRY_SUCCEED.getValue(),scBranchRecord.getRetryCount()+1);
                    }
                }catch (Exception e){
                    isAllPrepareSucceed = false;
                    updateBranchRecord(scBranchRecord.getBid(),TransStatus.TRY_FAILED.getValue(),scBranchRecord.getRetryCount()+1);
                    LOGGER.error("execute prepare error:{}",scBranchRecord,e);
                    break;
                }
            }
            if(isAllPrepareSucceed){
                globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),globalTransStatus.getValue(),TransStatus.TRY_SUCCEED.getValue(),scTransRecord.getRetryCount()+1);
                boolean isAllCommitSucceed=branchesCommit(scBranchRecordList);
                if(isAllCommitSucceed){
                    globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),globalTransStatus.getValue(),TransStatus.COMMIT_SUCCEED.getValue(),scTransRecord.getRetryCount()+1);
                }
                else{
                    globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),globalTransStatus.getValue(),TransStatus.COMMIT_FAILED.getValue(),scTransRecord.getRetryCount()+1);
                }
            }
            else{
                globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),globalTransStatus.getValue(),TransStatus.TRY_FAILED.getValue(),scTransRecord.getRetryCount()+1);
                boolean isAllCancelSucceed = branchesRollback(scBranchRecordList);
                if(isAllCancelSucceed){
                    globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),globalTransStatus.getValue(),TransStatus.CANCEL_SUCCEED.getValue(),scTransRecord.getRetryCount()+1);
                }
                else{
                    globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),globalTransStatus.getValue(),TransStatus.CANCEL_FAILED.getValue(),scTransRecord.getRetryCount()+1);
                }
            }
        } else if(globalTransStatus == TransStatus.TRY_FAILED || globalTransStatus ==TransStatus.CANCEL_FAILED ) {
            boolean isAllSucceed = branchesRollback(scTransRecord.getBranchTransactionList());
            if(isAllSucceed) {
                globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),null,TransStatus.CANCEL_SUCCEED.getValue(), scTransRecord.getRetryCount() + 1);
            }
            else{
                globalTransService.updateRetryCount(scTransRecord.getTid(),scTransRecord.getRetryCount() + 1);
            }
        }
        else if(globalTransStatus == TransStatus.TRY_SUCCEED||globalTransStatus == TransStatus.COMMIT_FAILED){
            boolean isAllSucceed=branchesCommit(scTransRecord.getBranchTransactionList());
            if(isAllSucceed) {
                globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),null,TransStatus.COMMIT_SUCCEED.getValue(), scTransRecord.getRetryCount() + 1);
            }
            else{
                globalTransService.updateRetryCount(scTransRecord.getTid(),scTransRecord.getRetryCount() + 1);
            }
        }

    }

    private ScResponseMessage processCall(String url,ScBranchRecord scBranchRecord,MessageType messageType) throws Exception {
        TransBranchInfo transBranchInfo = TransInfoBuilder.scBranchTransactionToBranchTransaction(scBranchRecord,TransMode.fromId(scBranchRecord.getTransMode()).name());
        ScRequestMessage scRequestMessage =  RpcRequestBuilder.buildBranchRequest(transBranchInfo,messageType);
        ScResponseMessage scResponseMessage =transactionProcessorFactory.callBranchActuator(url, scRequestMessage);
        return scResponseMessage;
    }

    private void updateBranchRecord(Long bid, int status, int retryCount) {
        Date now=new Date();
        branchTransService.updateStatusById(bid,status,retryCount,now);
    }


    private void processSagaTransaction(ScTransRecord scTransRecord, TransStatus globalTransStatus) {
        Map<String,Map<String,String>> returnParamMaps=new HashMap<>();
        if (globalTransStatus == TransStatus.READY||globalTransStatus == TransStatus.TRYING) {
            boolean isAllCommitSucceed=true;
            List<ScBranchRecord> scBranchRecordList=new ArrayList<>();
            for (ScBranchRecord scBranchRecord : scTransRecord.getBranchTransactionList()) {
                if(!scBranchRecordList.contains(scBranchRecord)){
                    scBranchRecordList.add(scBranchRecord);
                }
                if(scBranchRecord.getStatus()!=TransStatus.COMMIT_SUCCEED.getValue()) {
                    try {
                        boolean commitSucceed = branchSagaCommit(returnParamMaps, scBranchRecord);
                        if (commitSucceed) {
                            isAllCommitSucceed = false;
                            break;
                        }
                    } catch (Exception e) {
                        isAllCommitSucceed = false;
                        LOGGER.error("execute commit error:{}", scBranchRecord, e);
                        break;
                    }
                }
            }
            if(isAllCommitSucceed){
                globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),globalTransStatus.getValue(),TransStatus.COMMIT_SUCCEED.getValue(),scTransRecord.getRetryCount()+1);
            }
            else{
                globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),globalTransStatus.getValue(),TransStatus.COMMIT_FAILED.getValue(),scTransRecord.getRetryCount()+1);
                boolean isAllCancelSucceed = branchSagaRollback(scBranchRecordList,returnParamMaps);
                if(isAllCancelSucceed){
                    globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),globalTransStatus.getValue(),TransStatus.CANCEL_SUCCEED.getValue(),scTransRecord.getRetryCount()+1);
                }
                else{
                    globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),globalTransStatus.getValue(),TransStatus.CANCEL_FAILED.getValue(),scTransRecord.getRetryCount()+1);
                }
            }
        }
        else if(globalTransStatus == TransStatus.CANCEL_FAILED ||globalTransStatus == TransStatus.COMMIT_FAILED) {
            boolean isAllSucceed = branchSagaRollback(scTransRecord.getBranchTransactionList(),returnParamMaps);
            if(isAllSucceed) {
                globalTransService.updateStatusByTidAndStatus(scTransRecord.getTid(),null,TransStatus.CANCEL_SUCCEED.getValue(), scTransRecord.getRetryCount() + 1);
            }
            else{
                globalTransService.updateRetryCount(scTransRecord.getTid(),scTransRecord.getRetryCount() + 1);
            }
        }
    }

    private boolean branchSagaCommit(Map<String, Map<String, String>> returnParamMaps, ScBranchRecord scBranchRecord) {
        boolean isSucceed=true;
        try{
            Map<String, Object> mapRequest = RpcRequestBuilder.buildSagaBranchMapRequest(scBranchRecord, MessageType.TYPE_BRANCH_COMMIT);
            Map<String, String> requestParams = new HashMap<>();
            makeRequestParam(returnParamMaps, scBranchRecord.getExternalParam(), requestParams);
            if (scBranchRecord.getBranchParam() != null) {
                requestParams.putAll(JsonUtil.toMap(scBranchRecord.getBranchParam()));
            }
            mapRequest.put("requestParams", requestParams);
            transactionProcessorFactory.doCommit(scBranchRecord, returnParamMaps, mapRequest, scBranchRecord.getRetryCount() + 1);
            updateBranchRecord(scBranchRecord.getBid(),TransStatus.COMMIT_SUCCEED.getValue(),scBranchRecord.getRetryCount()+1);
        }catch (Exception e){
            LOGGER.error("execute saga commit error:{}",scBranchRecord,e);
            updateBranchRecord(scBranchRecord.getBid(),TransStatus.COMMIT_FAILED.getValue(),scBranchRecord.getRetryCount()+1);
            isSucceed=false;
        }
        return isSucceed;
    }

    private boolean branchSagaRollback(List<ScBranchRecord> scBranchRecordList, Map<String,Map<String,String>> returnParamMaps) {
        boolean isAllSucceed=true;
        for(ScBranchRecord scBranchRecord:scBranchRecordList) {
            if(scBranchRecord.getStatus()!=TransStatus.READY.getValue()&&scBranchRecord.getStatus()!=TransStatus.COMMIT_SUCCEED.getValue()) {
                try {
                    Map<String, String> requestParams = new HashMap<>();
                    Map<String, Object> mapRequest = RpcRequestBuilder.buildSagaBranchMapRequest(scBranchRecord, MessageType.TYPE_BRANCH_ROLLBACK);
                    makeRequestParam(returnParamMaps, scBranchRecord.getExternalParam(), requestParams);
                    if (scBranchRecord.getBranchParam() != null) {
                        requestParams.putAll(JsonUtil.toMap(scBranchRecord.getBranchParam()));
                    }
                    mapRequest.put("requestParams", requestParams);
                    callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_ROLLBACK_NAME), mapRequest);
                    updateBranchRecord(scBranchRecord.getBid(),TransStatus.CANCEL_SUCCEED.getValue(),scBranchRecord.getRetryCount()+1);
                } catch (Exception e) {
                    LOGGER.error("execute saga rollback error:{}", scBranchRecord, e);
                    updateBranchRecord(scBranchRecord.getBid(),TransStatus.CANCEL_FAILED.getValue(),scBranchRecord.getRetryCount()+1);
                    isAllSucceed = false;
                }
            }
        }
        return isAllSucceed;
    }

    private boolean branchesCommit(List<ScBranchRecord> scBranchRecordList) {
        boolean isAllCommitSucceed=true;
        for(ScBranchRecord scBranchRecord: scBranchRecordList) {
            try{
                ScResponseMessage scResponseMessage = null;
                if(TransMode.TCC==TransMode.fromId(scBranchRecord.getTransMode())) {
                    scResponseMessage = processCall(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.TCC_COMMIT_NAME),scBranchRecord, MessageType.TYPE_BRANCH_COMMIT);
                }
                else if(TransMode.XA==TransMode.fromId(scBranchRecord.getTransMode())){
                    scResponseMessage = processCall(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.XA_COMMIT_NAME),scBranchRecord, MessageType.TYPE_BRANCH_COMMIT);
                }
                else if(TransMode.SAGA==TransMode.fromId(scBranchRecord.getTransMode())){
                    scResponseMessage = processCall(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_COMMIT_NAME),scBranchRecord, MessageType.TYPE_BRANCH_COMMIT);
                }
                if(!TransactionResponseCode.SUCCESS.getCode().equals(scResponseMessage.getResultInfo().code)){
                    updateBranchRecord(scBranchRecord.getBid(),TransStatus.COMMIT_FAILED.getValue(),scBranchRecord.getRetryCount()+1);
                    isAllCommitSucceed =false;
                }
                else{
                    updateBranchRecord(scBranchRecord.getBid(),TransStatus.COMMIT_SUCCEED.getValue(),scBranchRecord.getRetryCount()+1);
                }
            }catch (Exception e){
                isAllCommitSucceed = false;
                updateBranchRecord(scBranchRecord.getBid(),TransStatus.COMMIT_FAILED.getValue(),scBranchRecord.getRetryCount()+1);
                LOGGER.error("execute rollback error:{}",scBranchRecord,e);
                break;
            }
        }
        return isAllCommitSucceed;
    }

    private boolean branchesRollback(List<ScBranchRecord> scBranchRecordList) {
        boolean isAllSucceed=true;
        for (ScBranchRecord scBranchRecord : scBranchRecordList) {
            try {
                TransStatus branchStatus = TransStatus.getTransStatusByValue(scBranchRecord.getStatus());
                if (branchStatus != TransStatus.CANCEL_SUCCEED && branchStatus != TransStatus.READY) {
                    ScResponseMessage scResponseMessage = null;
                    if(TransMode.TCC==TransMode.fromId(scBranchRecord.getTransMode())) {
                        scResponseMessage = processCall(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.TCC_ROLLBACK_NAME),scBranchRecord, MessageType.TYPE_BRANCH_ROLLBACK);
                    }
                    else if(TransMode.XA==TransMode.fromId(scBranchRecord.getTransMode())){
                        scResponseMessage = processCall(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.XA_ROLLBACK_NAME),scBranchRecord, MessageType.TYPE_BRANCH_ROLLBACK);
                    }
                    else if(TransMode.SAGA==TransMode.fromId(scBranchRecord.getTransMode())){
                        scResponseMessage = processCall(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_ROLLBACK_NAME),scBranchRecord, MessageType.TYPE_BRANCH_ROLLBACK);
                    }
                    if (!TransactionResponseCode.SUCCESS.getCode().equals(scResponseMessage.getResultInfo().code)) {
                        updateBranchRecord(scBranchRecord.getBid(),TransStatus.CANCEL_FAILED.getValue(),scBranchRecord.getRetryCount()+1);
                        isAllSucceed = false;
                    }
                    else{
                        updateBranchRecord(scBranchRecord.getBid(),TransStatus.CANCEL_SUCCEED.getValue(),scBranchRecord.getRetryCount()+1);
                    }
                }
            } catch (Exception e) {
                isAllSucceed = false;
                updateBranchRecord(scBranchRecord.getBid(),TransStatus.CANCEL_FAILED.getValue(),scBranchRecord.getRetryCount()+1);
                LOGGER.error("execute rollback error:{}", scBranchRecord, e);
            }
        }
        return isAllSucceed;
    }


    private  BranchTransResponse branchPrepare(final BranchTransRequest request){
        LOGGER.info("Prepare BranchTrans tid : {}", request.getTid());
        LOGGER.debug("Prepare Request : {}", request.toString());
        try {
            checkRequestParams(request);
            ScBranchRecord scBranchRecord = branchTransService.findByTidAndBid(Long.parseLong(request.getTid()), Long.parseLong(request.getBid()));
            List<ScBranchRecord> scBranchRecordList=new ArrayList<>();
            scBranchRecordList.add(scBranchRecord);
            setUrls(scBranchRecordList);
            TransBranchInfo transBranchInfo = TransInfoBuilder.scBranchTransactionToBranchTransaction(scBranchRecord,TransMode.fromId(request.getOperateType().getValue()).name());
            ScRequestMessage scRequestMessage =  RpcRequestBuilder.buildBranchRequest(transBranchInfo,MessageType.TYPE_BRANCH_PREPARE);
            ScResponseMessage scResponseMessage = null;
            if(TransMode.TCC==TransMode.valueOf(transBranchInfo.getTransMode())) {
                 scResponseMessage = transactionProcessorFactory.callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.TCC_PREPARE_NAME), scRequestMessage);
            }
            else if(TransMode.XA==TransMode.valueOf(transBranchInfo.getTransMode())){
                scResponseMessage = transactionProcessorFactory.callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.XA_PREPARE_NAME), scRequestMessage);
            }
            Map<String, String> externalParamsMap = new HashMap<>();
            transactionProcessorFactory.makeBranchResponse(externalParamsMap,transBranchInfo, scResponseMessage);
        }catch (Exception e){
            LOGGER.debug("Execute Prepare Request failed: {}", request);
            ParentResponse parentResponse=ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Failed.name()).build();
            return BranchTransResponse.newBuilder().setBranchId(request.getBid()).setBusinessId(request.getBid()).setBaseResponse(parentResponse).build();
        }
        ParentResponse parentResponse=ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Success.name()).build();
        return BranchTransResponse.newBuilder().setBranchId(request.getBid()).setBusinessId(request.getBid()).setBaseResponse(parentResponse).build();
    }


    private  BranchTransResponse branchCommit(final BranchTransRequest request){
        LOGGER.info("Commit BranchTrans tid : {}", request.getTid());
        LOGGER.debug("Commit Request : {}", request.toString());
        try {
            Map<String,Map<String,String>> returnParamMaps=new HashMap<>();
            checkRequestParams(request);
            ScBranchRecord scBranchRecord = branchTransService.findByTidAndBid(Long.parseLong(request.getTid()), Long.parseLong(request.getBid()));
            TransBranchInfo transBranchInfo = TransInfoBuilder.scBranchTransactionToBranchTransaction(scBranchRecord, TransMode.fromId(request.getOperateType().getValue()).name());
            if(TransMode.fromId(scBranchRecord.getTransMode())==TransMode.SAGA){
                Map<String,Object> mapRequest = RpcRequestBuilder.buildSagaBranchMapRequest(transBranchInfo, MessageType.TYPE_BRANCH_COMMIT);
                TransStatus transStatus=branchSagaTransCall(scBranchRecord,mapRequest,returnParamMaps);
                if(transStatus==TransStatus.COMMIT_FAILED){
                    ParentResponse parentResponse=ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Failed.name()).build();
                    return BranchTransResponse.newBuilder().setBranchId(request.getBid()).setBusinessId(request.getBid()).setBaseResponse(parentResponse).build();
                }
            }
            else {
                ScRequestMessage scRequestMessage = RpcRequestBuilder.buildBranchRequest(transBranchInfo, MessageType.TYPE_BRANCH_COMMIT);
                ScResponseMessage scResponseMessage=null;
                if(TransMode.fromId(scBranchRecord.getTransMode())==TransMode.TCC) {
                     scResponseMessage = transactionProcessorFactory.callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.TCC_COMMIT_NAME), scRequestMessage);
                }
                else if(TransMode.fromId(scBranchRecord.getTransMode())==TransMode.XA){
                    scResponseMessage = transactionProcessorFactory.callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.XA_COMMIT_NAME), scRequestMessage);
                }
                //transactionProcessorFactory.makeBranchResponse(externalParamsMap,transBranchInfo,scResponseMessage);
                if(!TransactionResponseCode.SUCCESS.getCode().equals(scResponseMessage.getResultInfo().code)){
                    ParentResponse parentResponse = ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Failed.name()).build();
                    return BranchTransResponse.newBuilder().setBranchId(request.getBid()).setBusinessId(request.getBid()).setBaseResponse(parentResponse).build();
                }
            }
        }catch (Exception e){
            LOGGER.debug("Execute commit Request failed: {}", request);
            ParentResponse parentResponse=ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Failed.name()).build();
            return BranchTransResponse.newBuilder().setBranchId(request.getBid()).setBusinessId(request.getBid()).setBaseResponse(parentResponse).build();
        }
        ParentResponse parentResponse=ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Success.name()).build();
        return BranchTransResponse.newBuilder().setBranchId(request.getBid()).setBusinessId(request.getBid()).setBaseResponse(parentResponse).build();
    }

    private BranchTransResponse branchRollback(final BranchTransRequest request){
        LOGGER.info("Rollback BranchTrans tid : {}", request.getTid());
        LOGGER.debug("Rollback Request : {}", request.toString());
        try {
            checkRequestParams(request);
            ScResponseMessage scResponseMessage=null;
            ScBranchRecord scBranchRecord = branchTransService.findByTidAndBid(Long.parseLong(request.getTid()), Long.parseLong(request.getBid()));
            TransBranchInfo transBranchInfo = TransInfoBuilder.scBranchTransactionToBranchTransaction(scBranchRecord,TransMode.fromId(request.getOperateType().getValue()).name());
            if(TransMode.fromId(scBranchRecord.getTransMode())==TransMode.SAGA){
                try {
                    Map<String,Object> mapRequest = RpcRequestBuilder.buildSagaBranchMapRequest(transBranchInfo, MessageType.TYPE_BRANCH_ROLLBACK);
                    callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_ROLLBACK_NAME),  mapRequest);
                } catch (Exception se) {
                    LOGGER.error("rollback failed, shift to async rollback:{} ", scBranchRecord, se);
                    ParentResponse parentResponse = ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Failed.name()).build();
                    return BranchTransResponse.newBuilder().setBranchId(request.getBid()).setBusinessId(request.getBid()).setBaseResponse(parentResponse).build();
                }
            }
            else {
                ScRequestMessage scRequestMessage = RpcRequestBuilder.buildBranchRequest(transBranchInfo, MessageType.TYPE_BRANCH_ROLLBACK);
                if(TransMode.fromId(scBranchRecord.getTransMode())==TransMode.TCC) {
                    scResponseMessage = transactionProcessorFactory.callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.TCC_ROLLBACK_NAME), scRequestMessage);
                }
                else if(TransMode.fromId(scBranchRecord.getTransMode())==TransMode.XA){
                    scResponseMessage = transactionProcessorFactory.callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.XA_ROLLBACK_NAME), scRequestMessage);
                }
                if(!TransactionResponseCode.SUCCESS.getCode().equals(scResponseMessage.getResultInfo().code)) {
                    ParentResponse parentResponse = ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Failed.name()).build();
                    return BranchTransResponse.newBuilder().setBranchId(request.getBid()).setBusinessId(request.getBid()).setBaseResponse(parentResponse).build();
                }
            }

        }catch (Exception e){
            LOGGER.debug("Execute rollback Request failed: {}", request);
            ParentResponse parentResponse=ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Failed.name()).build();
            return BranchTransResponse.newBuilder().setBranchId(request.getBid()).setBusinessId(request.getBid()).setBaseResponse(parentResponse).build();
        }
        ParentResponse parentResponse=ParentResponse.newBuilder().setBusinessId(request.getBusinessId()).setCode(ResultCode.Success.name()).build();
        return BranchTransResponse.newBuilder().setBranchId(request.getBid()).setBusinessId(request.getBid()).setBaseResponse(parentResponse).build();
    }

    private TransStatus branchSagaTransCall(ScBranchRecord scBranchRecord, Map<String,Object> mapRequest, Map<String,Map<String,String>> responseParamMaps) throws SQLException, ScServerException {
        Map<String,String> requestParams=new HashMap<>();
        makeRequestParam(responseParamMaps, scBranchRecord.getExternalParam(), requestParams);
        if(scBranchRecord.getBranchParam()!=null) {
            requestParams.putAll(JsonUtil.toMap(scBranchRecord.getBranchParam()));
        }
        mapRequest.put("requestParams",requestParams);
        return branchSagaCommitSingleSynCall(scBranchRecord,responseParamMaps, mapRequest);
    }


    private TransStatus branchSagaCommitSingleSynCall(ScBranchRecord scBranchRecord, Map<String,Map<String,String>> responseParamMaps, Map<String,Object> mapRequest){
        try{
            Map<String, String> returnParamMap = callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_COMMIT_NAME), mapRequest);
            if (returnParamMap != null) {
                responseParamMaps.put(scBranchRecord.getBranchName(), returnParamMap);
            }
        }catch (Exception e) {
            LOGGER.error("branch call error:{}, start to rollback", scBranchRecord, e);
            return TransStatus.COMMIT_FAILED;
        }
        return TransStatus.COMMIT_SUCCEED;
    }


    private void checkRequestParams(TransCompensationRequest request) {
        if (StringUtils.isBlank(request.getAppName())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "appName is empty!");
        }

        if (StringUtils.isEmpty(request.getTid())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "tid is empty!");
        }

        if (StringUtils.isEmpty(request.getCallerIp())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "callerIp is empty!");
        }

    }


    private void checkRequestParams(BranchTransRequest request) {
        if (StringUtils.isBlank(request.getBranchTransName())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "appName is empty!");
        }

        if (StringUtils.isEmpty(request.getTid())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "tid is empty!");
        }

        if (StringUtils.isEmpty(request.getCallerIp())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "callerIp is empty!");
        }

    }

    private void makeRequestParam(Map<String, Map<String, String>> responseParamMaps, String externalParam, Map<String, String> requestParams) {
        Map<String, String> externalParamMap=null;
        if(externalParam!=null){
            externalParamMap=JsonUtil.toMap(externalParam);
        }
        if(externalParamMap!=null&&!externalParamMap.isEmpty()){
            for(Map.Entry<String,String> entry:externalParamMap.entrySet()){
                Map<String,String> branchResponseParamMap=responseParamMaps.get(entry.getValue());
                if(branchResponseParamMap!=null){
                    requestParams.put(entry.getValue(),branchResponseParamMap.get(entry.getKey()));
                }
            }
        }
    }



    @Override
    @SuppressWarnings("unchecked")
    public void processGlobalTrans(final GlobalTransRequest request, StreamObserver responseObserver) {
        LOGGER.info("start to process the global transaction:{}", request.getBusinessId());
        try {
            GlobalTransRequest checkedRequest = checkGlobalTransParams(request);
            long tid = DistributeIdGenerator.generateId();
            ScTransRecord scTransactionInfo = TransInfoBuilder.buildGlobalTransInfo(tid, checkedRequest);
            setUrls(scTransactionInfo.getBranchTransactionList());
            if(findScTransaction(scTransactionInfo.getBusinessId())){
                throw new ScServerException(TransactionResponseCode.TRANSACTION_IS_EXIST,"the transaction is existing:"+scTransactionInfo.getBusinessId());
            }
            insertGlobalAndBranchTrans(scTransactionInfo);
            LOGGER.info("Global Transaction tid : {}, businessId : {}", scTransactionInfo.getTid(), scTransactionInfo.getBusinessId());
            TransactionInfo transactionInfo=TransInfoBuilder.scTransRecordToTransactionInfo(scTransactionInfo);
            transactionInfo.setStreamObserver(responseObserver);
            transExecuteService.submitGlobalTrans(transactionInfo);
        } catch (ScServerException se) {
            LOGGER.error("execute global transaction failed:{}",request,se);
            handleException(responseObserver, se.getServerResponseErrorCode(), request.getBusinessId(), se.getMessage());
        } catch (Throwable e) {
            LOGGER.error("Execute global transaction error:{}",request, e);
            handleException(responseObserver, TransactionResponseCode.TRANSACTION_PROCESS_FAILED, request.getBusinessId(), e.getMessage());
        }
    }

    private void setUrls(List<ScBranchRecord> scBranchRecordList) {
        for (ScBranchRecord scBranchRecord : scBranchRecordList) {
            Map<String, String> urlMap = new HashMap<>();
            if (TransMode.SAGA == TransMode.fromId(scBranchRecord.getTransMode())) {
                urlMap.put(ServerConstants.HttpAction.SAGA_COMMIT_NAME, TaServiceRecorder.getClientUrl(scBranchRecord.getBranchName(), ServerConstants.HttpAction.SAGA_COMMIT_NAME));
                urlMap.put(ServerConstants.HttpAction.SAGA_ROLLBACK_NAME, TaServiceRecorder.getClientUrl(scBranchRecord.getBranchName(), ServerConstants.HttpAction.SAGA_ROLLBACK_NAME));
                scBranchRecord.setUrlMap(urlMap);
            } else if (TransMode.TCC ==  TransMode.fromId(scBranchRecord.getTransMode())) {
                urlMap.put(ServerConstants.HttpAction.TCC_PREPARE_NAME, TaServiceRecorder.getClientUrl(scBranchRecord.getBranchName(), ServerConstants.HttpAction.TCC_PREPARE_NAME));
                urlMap.put(ServerConstants.HttpAction.TCC_COMMIT_NAME, TaServiceRecorder.getClientUrl(scBranchRecord.getBranchName(), ServerConstants.HttpAction.TCC_COMMIT_NAME));
                urlMap.put(ServerConstants.HttpAction.TCC_ROLLBACK_NAME, TaServiceRecorder.getClientUrl(scBranchRecord.getBranchName(), ServerConstants.HttpAction.TCC_ROLLBACK_NAME));
                scBranchRecord.setUrlMap(urlMap);
            } else if (TransMode.XA ==  TransMode.fromId(scBranchRecord.getTransMode())) {
                urlMap.put(ServerConstants.HttpAction.XA_PREPARE_NAME, TaServiceRecorder.getClientUrl(scBranchRecord.getBranchName(), ServerConstants.HttpAction.XA_PREPARE_NAME));
                urlMap.put(ServerConstants.HttpAction.XA_COMMIT_NAME, TaServiceRecorder.getClientUrl(scBranchRecord.getBranchName(), ServerConstants.HttpAction.XA_COMMIT_NAME));
                urlMap.put(ServerConstants.HttpAction.XA_ROLLBACK_NAME, TaServiceRecorder.getClientUrl(scBranchRecord.getBranchName(), ServerConstants.HttpAction.XA_ROLLBACK_NAME));
                scBranchRecord.setUrlMap(urlMap);
            }
            if(!urlMap.isEmpty()) {
                scBranchRecord.setUrl(JsonUtil.toString(urlMap));
            }
        }
    }


    @Override
    public void processGlobalSagaTrans(GlobalSagaTransRequest request, StreamObserver responseObserver) throws ScTransactionException {
        LOGGER.info("start to process the global saga transaction:{}", request.getBusinessId());
        try {
            GlobalSagaTransRequest checkedRequest = checkGlobalTransParams(request);
            long tid = DistributeIdGenerator.generateId();
            ScTransRecord scTransRecord=TransInfoBuilder.globalSagaToScTransRecord(tid, checkedRequest);
            setUrls(scTransRecord.getBranchTransactionList());
            if(findScTransaction(scTransRecord.getBusinessId())){
                throw new ScServerException(TransactionResponseCode.TRANSACTION_IS_EXIST,"the transaction is existing:"+scTransRecord.getBusinessId());
            }
            insertGlobalAndBranchTrans(scTransRecord);
            LOGGER.info("Global Transaction tid : {}, businessId : {}", tid, request.getBusinessId());
            TransactionInfo transactionInfo = TransInfoBuilder.scTransRecordToTransactionInfo(scTransRecord);
            transactionInfo.setStreamObserver(responseObserver);
            transExecuteService.submitGlobalTrans(transactionInfo);
        }catch(ScServerException se){
            LOGGER.error("execute global saga transaction failed:{}",request,se);
            handleException(responseObserver, se.getServerResponseErrorCode(), request.getBusinessId(), se.getMessage());
        }catch (Throwable e){
            LOGGER.error("execute global saga transaction failed:{}",request,e);
            handleException(responseObserver, TransactionResponseCode.TRANSACTION_PROCESS_FAILED, request.getBusinessId(), e.getMessage());
        }
    }

    private void handleException(StreamObserver responseObserver, TransactionResponseCode serverResponseErrorCode, String businessId, String message) {
        ParentResponse parentResponse;
        parentResponse = RpcResponseBuilder.buildErrorBaseResponse(serverResponseErrorCode, businessId, message);
        responseObserver
                .onNext(GlobalTransResponse.newBuilder().setBaseResponse(parentResponse).build());
        responseObserver.onCompleted();
    }


    private boolean findScTransaction(String businessId) {
        ScTransRecord scTransRecord=globalTransService.getByBusinessId(businessId);
        return scTransRecord==null?false:true;
    }


    private void insertGlobalAndBranchTrans(ScTransRecord transactionInfo){
        globalTransService.saveTransAndBranchTrans(transactionInfo);
    }



    private void updateBranchRecord(Long tid, int status, int retryCount,Date modifyTime) {
        branchTransService.updateStatusById(tid,status,retryCount,modifyTime);
    }


    private void updateScTransRecord(Long tid, int status, int retryCount) {
        globalTransService.updateStatusByTidAndStatus(tid,status,retryCount);
    }


    @Override
    public void processGlobalSagaRollback(GlobalTransRollbackRequest request, StreamObserver responseObserver) throws ScTransactionException{
         ScTransRecord scTransRecord=globalTransService.getByBusinessId(request.getBusinessId());
         if(scTransRecord==null){
             LOGGER.warn("the transaction is not existed:"+request.getBusinessId());
             throw new ScTransactionException(TransactionResponseCode.TRANS_NOT_EXIST,"the transaction is not existed:"+request.getBusinessId());
         }
         if(request.getIsSync()){
             for(ScBranchRecord scBranchRecord:scTransRecord.getBranchTransactionList()){
                 try {
                     rollbackSaga(scBranchRecord);
                     updateBranchRecord(scBranchRecord.getBid(),TransStatus.CANCEL_SUCCEED.getValue(), scBranchRecord.getRetryCount()+1,new Date());
                 }catch (Exception e){
                     LOGGER.error("rollback the transaction error:{}",scBranchRecord,e);
                     updateBranchRecord(scBranchRecord.getBid(),TransStatus.CANCEL_FAILED.getValue(), scBranchRecord.getRetryCount()+1,new Date());
                     throw new ScTransactionException(TransactionResponseCode.TRANSACTION_ROLLBACK_FAILED,"the transaction rollback failed:"+request.getBusinessId());
                 }
             }
             updateScTransRecord(scTransRecord.getTid(),TransStatus.CANCEL_SUCCEED.getValue(), scTransRecord.getRetryCount()+1);
         }
         else{
              rollbackSagaInParallel(scTransRecord);
         }
    }

    @Override
    public void processGlobalRollback(GlobalTransRollbackRequest request, StreamObserver responseObserver) {

    }

    @Override
    public TransQueryResponse findGlobalTrans(TransQueryRequest request) {
        ScTransRecord scTransRecord=globalTransService.getByBusinessId(request.getBusinessId());
        if(scTransRecord!=null){
            List<ScBranchRecord> scBranchRecordList = branchTransService.getTransBranchInfoList(scTransRecord.getTid());
            if(scBranchRecordList!=null){
                scTransRecord.setBranchTransactionList(scBranchRecordList);
            }
        }
        else{
            throw new ScTransactionException(TransactionResponseCode.TRANS_NOT_EXIST,"transaction is not exist!");
        }
        TransQueryResponse.Builder builder = RpcResponseBuilder.buildTransQueryResponse(scTransRecord);
        builder.setBaseResponse(RpcResponseBuilder.buildSuccessBaseResponse(request.getBusinessId()));
        return builder.build();
    }

    private void rollbackSagaInParallel(ScTransRecord scTransRecord) {
        CountDownLatch countDownLatch=new CountDownLatch(scTransRecord.getBranchTransactionList().size());
        Map<String,Boolean> resultMap=new HashMap<>();
        for(ScBranchRecord scBranchRecord:scTransRecord.getBranchTransactionList()) {
            resultMap.put(scBranchRecord.getBranchName(),true);
            transactionExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Map<String,Object> mapRequest = RpcRequestBuilder.buildSagaBranchMapRequest(scBranchRecord, MessageType.TYPE_BRANCH_COMMIT);
                        callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_ROLLBACK_NAME), mapRequest);
                    }catch (Exception e){
                        resultMap.put(scBranchRecord.getBranchName(),false);
                        LOGGER.error("execute saga rollback branch error:{}",scBranchRecord,e);
                    }
                    finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await(scTransRecord.getTimeout(), TimeUnit.MILLISECONDS);
        }catch (InterruptedException ie){
            LOGGER.warn("wait tcc branch prepare error!",ie);
        }
        for (ScBranchRecord scBranchRecord : scTransRecord.getBranchTransactionList()){
            if(!resultMap.get(scBranchRecord.getBranchName())){
                updateBranchRecord(scBranchRecord.getBid(),TransStatus.CANCEL_FAILED.getValue(), scBranchRecord.getRetryCount()+1,new Date());
                throw new ScTransactionException(TransactionResponseCode.BRANCH_ROLLBACK_FAILED,"rollback saga branch failed:"+scBranchRecord.getBranchName());
            }
            updateBranchRecord(scBranchRecord.getBid(),TransStatus.CANCEL_SUCCEED.getValue(), scBranchRecord.getRetryCount()+1,new Date());
        }
        updateScTransRecord(scTransRecord.getTid(),TransStatus.CANCEL_SUCCEED.getValue(), scTransRecord.getRetryCount()+1);
    }

    private void rollbackSaga(ScBranchRecord scBranchRecord) throws ScServerException{
        Map<String,Object> mapRequest = RpcRequestBuilder.buildSagaBranchMapRequest(scBranchRecord, MessageType.TYPE_BRANCH_COMMIT);
        callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_ROLLBACK_NAME),mapRequest);
    }


    private void insertScTransRecord(ScTransRecord scTransactionInfo){
        globalTransService.save(scTransactionInfo);
        branchTransService.save(scTransactionInfo.getBranchTransactionList());
    }



    // 本地模式只需要修改tid为终态即可即可
    private TransStatus getTransStatus(TransStatus transStatus, ServerMode mode) {
        TransStatus newStatus;
        if (ServerMode.REMOTE == mode) {
            newStatus = transStatus.equals(TransStatus.TRY_SUCCEED) ? TransStatus.COMMITTING : TransStatus.CANCELLING;
        } else {
            newStatus = transStatus.equals(TransStatus.TRY_SUCCEED) ? TransStatus.COMMIT_SUCCEED
                    : TransStatus.CANCEL_SUCCEED;
        }
        return newStatus;
    }

    /**
     * 发送混合事务消息
     *
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public RegTransMsgResponse prepareTransMsg(final RegTransMsgRequest request) {

        if (org.springframework.util.StringUtils.isEmpty(request.getTid())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "Tid is empty!");
        }
        if (request.getProducerId() == null) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "ProducerId is empty!");
        }

        ScTransRecord transactionInfo = globalTransService.getByTid(Long.parseLong(request.getTid()));

        if (transactionInfo == null) {
            throw new ScServerException(TransactionResponseCode.TRANS_NOT_EXIST,"transaction is not exist!");
        }

        if (!TransStatus.TRYING.getValue().equals(transactionInfo.getStatus())) {
            throw new ScServerException(TransactionResponseCode.GLOBAL_TRANS_OPERATE_ILLEGAL,"transaction status is illegal!");
        }
        Long branchId = DistributeIdGenerator.generateId();
        LOGGER.info("PrepareTransMsg tid : {},produceId : {}", request.getTid(), request.getProducerId());
        LOGGER.debug("PrepareTransMsg dto : {}", request.toString());

        //branch2PCTransaction.saveTransInfo(TransBranchInfoBuilder.getEnhancedTransBranchInfo(request, branchId));

        RegTransMsgResponse.Builder builder = RegTransMsgResponse.newBuilder();
        builder.setBaseResponse(RpcResponseBuilder.buildSuccessBaseResponse(request.getRequestId()));
        builder.setBranchId(String.valueOf(branchId));
        return builder.build();
    }

    /**
     * 生产者注册
     *
     * @return
     */
    @Override
    public MQProducerRegResponse registerProducer(final MQProducerRegRequest request) {

        if (StringUtils.isBlank(request.getConfig())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "Config is empty!");
        }
        if (!request.hasType()) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "Type is empty!");
        }
        long producerId = metaService.getMqProducerId(request);
        LOGGER.info("RegisterProducer : {} and ProducerId : {}", request.toString(), producerId);
        MQProducerRegResponse.Builder builder = MQProducerRegResponse.newBuilder();
        builder.setBaseResponse(RpcResponseBuilder.buildSuccessBaseResponse(request.getRequestId()));
        builder.setProducerId(String.valueOf(producerId));
        return builder.build();
    }

    /**
     * 本地模式StateCheck
     *
     * @param request
     * @return
     */
    @Override
    public StateServiceResponse stateCheck(StateServiceRequest request) {
        ScTransRecord transactionInfo = globalTransService.getByTid(Long.parseLong(request.getTid()));
        if (transactionInfo == null) {
            throw new ScServerException(TransactionResponseCode.TRANS_NOT_EXIST,"transaction is not exist!");
        }
        StateServiceResponse.Builder builder = StateServiceResponse.newBuilder();
        builder.setBaseResponse(RpcResponseBuilder.buildSuccessBaseResponse(request.getBusinessId()));
        builder.setStatus(UInt32Value.of(transactionInfo.getStatus()));
        return builder.build();
    }



    private boolean isAllBranchSuccess(TransBranchInfo transBranchInfo) {
        boolean result = true;
        List<ScBranchRecord> branchInfoList = branchTransService.getTransBranchInfoList(transBranchInfo.getTid());
        for (ScBranchRecord branchInfo : branchInfoList) {
            if (branchInfo.getStatus().equals(transBranchInfo.getStatus())) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * 全局事务参数校验
     * @param request
     * @return
     */
    private GlobalTransRequest checkGlobalTransParams(final GlobalTransRequest request) {
        GlobalTransRequest.Builder builder = request.toBuilder();
        if (StringUtils.isBlank(builder.getAppName())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "appName is empty!");
        }
        if (StringUtils.isBlank(builder.getTransGroupId())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "groupId is empty!");
        }
        if (null == transGroupCache.getTransGroup(builder.getTransGroupId())) {
            throw new ScServerException(TransactionResponseCode.GROUP_NOT_EXIST,"Group is not exist!");
        }
        if (StringUtils.isBlank(builder.getCallerIp())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "callerIp is empty!");
        }
        if (!builder.hasTimeout()) {
            builder.setTimeout(UInt64Value.of(Constants.DEFAULT_TIMEOUT));
        } else if (builder.getTimeout().getValue() <= 0) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "timeout is empty!");
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


    private GlobalSagaTransRequest checkGlobalTransParams(final GlobalSagaTransRequest request) {
        GlobalSagaTransRequest.Builder builder = request.toBuilder();
        if (StringUtils.isBlank(builder.getAppName())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "appName is empty!");
        }
        if (StringUtils.isBlank(builder.getTransGroupId())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "transGroupId is empty!");
        }
        if (null == transGroupCache.getTransGroup(builder.getTransGroupId())) {
            throw new ScServerException(TransactionResponseCode.GROUP_NOT_EXIST,"group not exist!");
        }
        if (StringUtils.isBlank(builder.getCallerIp())) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "callerIp is empty!");
        }
        if (!builder.hasTimeout()) {
            builder.setTimeout(UInt64Value.of(Constants.DEFAULT_TIMEOUT));
        } else if (builder.getTimeout().getValue() <= 0) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "transTimeout is null!");
        }

        if (!builder.hasTimeoutType()) {
            builder.setTimeoutType(UInt32Value.of(TimeoutType.getDefault().getValue()));
        } else {
            TimeoutType.getByValue(builder.getTimeoutType().getValue());
        }
        return builder.build();
    }

}
