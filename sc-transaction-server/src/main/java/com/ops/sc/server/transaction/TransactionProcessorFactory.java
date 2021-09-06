package com.ops.sc.server.transaction;

import com.ops.sc.common.bean.*;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.constant.ServerConstants;
import com.ops.sc.common.enums.MessageType;
import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.exception.ScServerException;
import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.common.model.TransBranchInfo;
import com.ops.sc.common.model.TransactionInfo;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.thread.NamedThreadFactory;
import com.ops.sc.common.trans.BaseTwoPhaseTransaction;
import com.ops.sc.common.trans.TransCommonResponse;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.core.build.RpcRequestBuilder;
import com.ops.sc.core.gather.TransInfoBuilder;
import com.ops.sc.server.dao.TransGroupDao;
import com.ops.sc.server.service.CallAction;
import com.ops.sc.server.event.BranchTransEvent;
import com.ops.sc.server.event.GlobalTransEvent;
import com.ops.sc.server.eventbus.TranTraceEventBus;
import com.ops.sc.server.service.GlobalTransService;
import com.ops.sc.server.service.BranchTransService;
import com.ops.sc.server.service.CallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service("transactionProcessorFactory")
public class TransactionProcessorFactory {

        private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProcessorFactory.class);

        @Resource
        private TransGroupDao transGroupDao;

        @Resource(name = "globalTwoPhaseTransaction")
        private BaseTwoPhaseTransaction globalTwoPhaseTransaction;

        @Resource
        private GlobalTransService globalTransService;
        @Resource
        private BranchTransService branchTransService;

        @Resource
        private CallAction callAction;

        @Resource(name = "httpCallService")
        private CallService callService;

        @Autowired
        private TranTraceEventBus tranTraceEventBus;


        private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1,
            2*Runtime.getRuntime().availableProcessors(), 5, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new NamedThreadFactory("processorThread", 2*Runtime.getRuntime().availableProcessors()), new ThreadPoolExecutor.CallerRunsPolicy());


        public TransCommonResponse processGlobalTrans(TransactionInfo transactionInfo){
            switch (transactionInfo.getTransMode()){
                case TCC:
                    return processTcc(transactionInfo);
                case SAGA:
                    return processSaga(transactionInfo);
                case XA:
                    return processXA(transactionInfo);
                default:
                    LOGGER.error("not supported trans mode:{}",transactionInfo.getTransMode());
                    return TransCommonResponse.builder().errorMsg("not supported trans mode!").businessId(transactionInfo.getBusinessId()).errorCode(TransactionResponseCode.NOT_SUPPORTED_MODE.getCode()).status(TransCommonResponse.Status.FAILED).build();
                }
        }


    private TransCommonResponse processTcc(TransactionInfo transactionInfo){
        LOGGER.info("start to process the global transaction: {}",transactionInfo);
        if(transactionInfo.getCallInParallel()==0) {
            BranchResult prepareBranchResult = prepare(transactionInfo.getBranchTransactionList());
            if(prepareBranchResult.isSucceed){
                LOGGER.info("Execute tcc prepare succeed,start to commit, businessId: {}",transactionInfo.getBusinessId());
                try {
                    publishGlobalTransEvent(transactionInfo.getTid(),TransStatus.READY, TransStatus.TRY_SUCCEED);
                    boolean result=commit(prepareBranchResult.finishedBranchList);
                    if(!result){
                        publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.COMMIT_FAILED);
                        return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg("commit branch failed").build();
                    }
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.COMMIT_SUCCEED);
                    LOGGER.info("Execute tcc commit succeed: {}",transactionInfo);
                }catch (ScTransactionException ce){
                    LOGGER.error("Execute tcc commit failed: {}",transactionInfo,ce);
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.COMMIT_FAILED);
                    return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(ce.getMessage()).build();
                }
                return TransCommonResponse.builder().status(TransCommonResponse.Status.SUCCESS).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(null).build();
            }
            else{
                LOGGER.info("Execute tcc prepare failed,start to rollback, businessId: {}",transactionInfo.getBusinessId());
                try {
                    publishGlobalTransEvent(transactionInfo.getTid(),TransStatus.READY, TransStatus.TRY_FAILED);
                    boolean result=rollback(prepareBranchResult.finishedBranchList);
                    if(!result){
                        publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.CANCEL_FAILED);
                        return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg("Rollback branch failed").build();
                    }
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.CANCEL_SUCCEED);
                    LOGGER.info("Execute tcc rollback succeed: {}",transactionInfo);
                }catch (ScTransactionException re) {
                    LOGGER.error("Execute tcc rollback failed: {}",transactionInfo,re);
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.CANCEL_FAILED);
                    return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(re.getMessage()).build();
                }
                return TransCommonResponse.builder().status(TransCommonResponse.Status.ROLLEDBACK).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(null).build();
            }
        }
        else{
            try {
                prepareInParallel(transactionInfo);
            }catch (ScTransactionException e){
                LOGGER.error("Execute tcc prepare failed: {}",transactionInfo,e);
                try {
                    publishGlobalTransEvent(transactionInfo.getTid(),TransStatus.TRYING, TransStatus.TRY_FAILED);
                    rollbackInParallel(transactionInfo);
                    LOGGER.info("Tcc rollback succeed: {}",transactionInfo);
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.CANCEL_SUCCEED);
                }catch (ScTransactionException re){
                    LOGGER.error("Tcc rollback failed: {}",transactionInfo,re);
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.CANCEL_FAILED);
                }
                return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(e.getMessage()).build();
            }
            try {
                publishGlobalTransEvent(transactionInfo.getTid(),TransStatus.TRYING, TransStatus.TRY_SUCCEED);
                commitInParallel(transactionInfo);
                LOGGER.info("Tcc commit succeed: {}",transactionInfo);
                publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.COMMIT_SUCCEED);
                LOGGER.info("Succeed to finish global transaction, tid : {}, bizId : {}", transactionInfo.getTid(), transactionInfo.getBusinessId());
                return TransCommonResponse.builder().status(TransCommonResponse.Status.SUCCESS).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).build();
            }catch (ScTransactionException ce){
                LOGGER.error("Tcc commit failed: {}",transactionInfo,ce);
                publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.COMMIT_FAILED);
                return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(ce.getMessage()).build();
            }
        }
    }


    private TransCommonResponse processXA(TransactionInfo transactionInfo) throws ScTransactionException{
        LOGGER.info("start to process the global XA transaction: {}",transactionInfo);
        if(transactionInfo.getCallInParallel()==0) {
            BranchResult prepareBranchResult = prepare(transactionInfo.getBranchTransactionList());
            if(prepareBranchResult.isSucceed){
                try {
                    LOGGER.info("Execute XA prepare succeed,start to commit branch,businessId: {}",transactionInfo.getBusinessId());
                    publishGlobalTransEvent(transactionInfo.getTid(),TransStatus.READY, TransStatus.TRY_SUCCEED);
                    boolean result=commit(prepareBranchResult.finishedBranchList);
                    if(!result){
                        publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.COMMIT_FAILED);
                        return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg("commit branch failed").build();
                    }
                    LOGGER.error("Execute XA commit succeed: {}",transactionInfo);
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.COMMIT_SUCCEED);
                }catch (ScTransactionException re){
                    LOGGER.error("Execute XA commit failed: {}",transactionInfo,re);
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.COMMIT_FAILED);
                    return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(re.getMessage()).build();
                }
                return TransCommonResponse.builder().status(TransCommonResponse.Status.SUCCESS).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(null).build();
            }
            else{
                LOGGER.info("Execute xa prepare failed,start to rollback branch,businessId: {}",transactionInfo.getBusinessId());
                try {
                    publishGlobalTransEvent(transactionInfo.getTid(),TransStatus.READY, TransStatus.TRY_FAILED);
                    boolean result=rollback(prepareBranchResult.finishedBranchList);
                    if(!result){
                        publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.CANCEL_FAILED);
                        return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg("Rollback branch failed").build();
                    }
                    LOGGER.info("XA rollback succeed: {}",transactionInfo);
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.CANCEL_SUCCEED);
                }catch (ScTransactionException re){
                    LOGGER.error("XA rollback failed: {}",transactionInfo,re);
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.CANCEL_FAILED);
                    return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(re.getMessage()).build();
                }
                return TransCommonResponse.builder().status(TransCommonResponse.Status.ROLLEDBACK).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(null).build();
            }
        }
        else{
            try {
                prepareInParallel(transactionInfo);
            }catch (ScTransactionException e){
                LOGGER.error("Execute xa prepare failed: {}",transactionInfo,e);
                try {
                    publishGlobalTransEvent(transactionInfo.getTid(),TransStatus.TRYING, TransStatus.TRY_SUCCEED);
                    rollbackInParallel(transactionInfo);
                    LOGGER.info("XA rollback succeed: {}",transactionInfo);
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.CANCEL_SUCCEED);
                }catch (ScTransactionException re){
                    LOGGER.error("XA rollback failed: {}",transactionInfo,re);
                    publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.CANCEL_FAILED);
                }
                return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(e.getMessage()).build();
            }
            try {
                publishGlobalTransEvent(transactionInfo.getTid(),TransStatus.TRYING, TransStatus.TRY_SUCCEED);
                commitInParallel(transactionInfo);
                publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.COMMIT_SUCCEED);
                LOGGER.error("XA commit succeed: {}",transactionInfo);
            }catch (ScTransactionException ce){
                LOGGER.error("XA commit failed: {}",transactionInfo,ce);
                publishGlobalTransEvent(transactionInfo.getTid(),null, TransStatus.COMMIT_FAILED);
                return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).errorMsg(ce.getMessage()).build();
            }
        }
        LOGGER.info("Succeed to execute xa transaction  tid : {}, bizId : {}", transactionInfo.getTid(), transactionInfo.getBusinessId());
        return TransCommonResponse.builder().status(TransCommonResponse.Status.SUCCESS).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).build();
    }


    private BranchResult prepare(List<TransBranchInfo> branchTransactionList){
        List<TransBranchInfo> finishedBranchList=new ArrayList<>();
        Map<String, String> externalParamsMap = new HashMap<>();
        ScResponseMessage scResponseMessage = null;
        BranchResult branchResult =new BranchResult();
        branchResult.isSucceed=true;
        for (TransBranchInfo transBranchInfo : branchTransactionList) {
            try{
                if (transBranchInfo.getExternalMap() != null) {
                    for (String key : transBranchInfo.getExternalMap().keySet()) {
                        transBranchInfo.getParamMap().put(key, externalParamsMap.get(getParamKey(transBranchInfo.getBranchName(), key)));
                    }
                }
                ScRequestMessage scRequestMessage =null;
                scRequestMessage = RpcRequestBuilder.buildBranchRequest(transBranchInfo,MessageType.TYPE_BRANCH_PREPARE);
                scRequestMessage.setMessageType(MessageType.TYPE_BRANCH_PREPARE.getValue());
                if(TransMode.TCC==TransMode.valueOf(transBranchInfo.getTransMode())) {
                    scResponseMessage = callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.TCC_PREPARE_NAME), scRequestMessage);
                }
                else if(TransMode.XA==TransMode.valueOf(transBranchInfo.getTransMode())){
                    scResponseMessage = callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.XA_PREPARE_NAME), scRequestMessage);
                }
                if(scRequestMessage ==null){
                    throw new ScTransactionException("prepare call branch transaction actuator failed, businessId:" + transBranchInfo.getBusinessId() +",branchId:"+transBranchInfo.getBid());
                }
                if(!finishedBranchList.contains(transBranchInfo)) {
                    finishedBranchList.add(transBranchInfo);
                }
            }catch (Throwable e){
                LOGGER.error("prepare call branch transaction actuator failed: {}",transBranchInfo, e);
                if(!finishedBranchList.contains(transBranchInfo)) {
                    finishedBranchList.add(transBranchInfo);
                }
                branchResult.isSucceed=false;
                branchResult.finishedBranchList=finishedBranchList;
                publishBranchTransEvent(transBranchInfo.getBid(),TransStatus.TRYING,TransStatus.TRY_FAILED,0);
                return branchResult;
            }
            try {
                makeBranchResponse(externalParamsMap, transBranchInfo, scResponseMessage);
            }catch (Throwable e){
                LOGGER.error("prepare branch transaction failed: {}",transBranchInfo, e);
                branchResult.isSucceed=false;
                branchResult.finishedBranchList=finishedBranchList;
                return branchResult;
            }
         }
        branchResult.finishedBranchList=finishedBranchList;
        return branchResult;
    }

    private static class BranchResult {
            public boolean isSucceed;
            public List<TransBranchInfo> finishedBranchList;
    }


    private boolean commit(List<TransBranchInfo> finishedBranchList){
       return doCall(finishedBranchList, MessageType.TYPE_BRANCH_COMMIT, Constants.COMMIT);
    }

    private boolean rollback(List<TransBranchInfo> finishedBranchList){
        return doCall(finishedBranchList, MessageType.TYPE_BRANCH_ROLLBACK, Constants.CANCEL);
    }


    private boolean doCall(List<TransBranchInfo> finishedBranchList, MessageType messageType, int type) {
        Map<String, String> externalParamsMap = new HashMap<>();
        boolean isSucceed=true;
        for (TransBranchInfo transBranchInfo : finishedBranchList) {
            ScResponseMessage scResponseMessage = null;
            try {
                if (transBranchInfo.getExternalMap() != null) {
                    for (String key : transBranchInfo.getExternalMap().keySet()) {
                        transBranchInfo.getParamMap().put(key, externalParamsMap.get(getParamKey(transBranchInfo.getBranchName(), key)));
                    }
                }
                ScRequestMessage scRequestMessage = null;
                scRequestMessage = RpcRequestBuilder.buildBranchRequest(transBranchInfo, messageType);
                scRequestMessage.setMessageType(messageType.getValue());
                if(type==Constants.COMMIT) {
                    if(TransMode.TCC==TransMode.valueOf(transBranchInfo.getTransMode())) {
                        scResponseMessage = callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.TCC_COMMIT_NAME), scRequestMessage);
                    }
                    else if(TransMode.XA==TransMode.valueOf(transBranchInfo.getTransMode())){
                        scResponseMessage = callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.XA_COMMIT_NAME), scRequestMessage);
                    }
                    else if(TransMode.SAGA==TransMode.valueOf(transBranchInfo.getTransMode())){
                        scResponseMessage = callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.SAGA_COMMIT_NAME), scRequestMessage);
                    }
                }
                else if(type==Constants.CANCEL){
                    if(TransMode.TCC==TransMode.valueOf(transBranchInfo.getTransMode())) {
                        scResponseMessage = callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.TCC_ROLLBACK_NAME), scRequestMessage);
                    }
                    else if(TransMode.XA==TransMode.valueOf(transBranchInfo.getTransMode())){
                        scResponseMessage = callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.XA_ROLLBACK_NAME), scRequestMessage);
                    }
                    else if(TransMode.SAGA==TransMode.valueOf(transBranchInfo.getTransMode())){
                        scResponseMessage = callBranchActuator(transBranchInfo.getUrlMap().get(ServerConstants.HttpAction.SAGA_ROLLBACK_NAME), scRequestMessage);
                    }
                }
                if(scRequestMessage ==null){
                    throw new ScTransactionException("call branch transaction actuator failed, businessId:" + transBranchInfo.getBusinessId() +",branchId:"+transBranchInfo.getBid());
                }
                makeBranchResponse(externalParamsMap, transBranchInfo, scResponseMessage);
            } catch (Throwable e) {
                isSucceed=false;
                LOGGER.error("call branch transaction actuator failed: {}", transBranchInfo, e);
                publishBranchTransEvent(transBranchInfo.getBid(),null,messageType==MessageType.TYPE_BRANCH_COMMIT?TransStatus.COMMIT_FAILED:TransStatus.CANCEL_FAILED,0);
                //throw new ScTransactionException("call branch transaction actuator failed, businessId:" + transBranchInfo.getBusinessId() +",branchId:"+transBranchInfo.getBid(), e);
            }
        }
        return isSucceed;
    }




    public void makeBranchResponse(Map<String, String> externalParamsMap, TransBranchInfo transBranchInfo, ScResponseMessage scResponseMessage) {
            if (scResponseMessage.getReturnParamMap() != null && !scResponseMessage.getReturnParamMap().isEmpty()) {
                for (String key : scResponseMessage.getReturnParamMap().keySet()) {
                    externalParamsMap.put(getParamKey(transBranchInfo.getBranchName(), key), scResponseMessage.getReturnParamMap().get(key));
                }
            }
            MessageType messageType = MessageType.getByValue(scResponseMessage.getMessageType());
            switch (messageType){
                case TYPE_BRANCH_PREPARE_RESP:
                    if (TransactionResponseCode.SUCCESS.getCode().equals(scResponseMessage.getResultInfo().code)) {
                        publishBranchTransEvent(scResponseMessage.getBranchId(),TransStatus.TRYING,TransStatus.TRY_SUCCEED,0);
                    } else {
                        LOGGER.warn("Execute branch prepare error: {}", scResponseMessage);
                        publishBranchTransEvent(scResponseMessage.getBranchId(),TransStatus.TRYING,TransStatus.TRY_FAILED,0);
                        throw new ScTransactionException("Execute branch prepare error,branchId:" + scResponseMessage.getBranchId()+",businessId:"+ transBranchInfo.getBusinessId());
                    }
                    break;
                case TYPE_BRANCH_COMMIT_RESP:
                    if (TransactionResponseCode.SUCCESS.getCode().equals(scResponseMessage.getResultInfo().code)) {
                        publishBranchTransEvent(scResponseMessage.getBranchId(),null,TransStatus.COMMIT_SUCCEED,0);
                    } else {
                        LOGGER.error("Execute branch commit error: {}", scResponseMessage);
                        publishBranchTransEvent(scResponseMessage.getBranchId(),null,TransStatus.COMMIT_FAILED,0);
                        throw new ScTransactionException("Execute branch commit error,branchId:" + scResponseMessage.getBranchId()+",businessId:"+ transBranchInfo.getBusinessId());
                    }
                    break;
                case TYPE_BRANCH_ROLLBACK_RESP:
                    if (TransactionResponseCode.SUCCESS.getCode().equals(scResponseMessage.getResultInfo().code)) {
                        publishBranchTransEvent(scResponseMessage.getBranchId(),null,TransStatus.CANCEL_SUCCEED,0);
                    } else {
                        LOGGER.error("Execute branch rollback error: {}", scResponseMessage);
                        publishBranchTransEvent(scResponseMessage.getBranchId(),null,TransStatus.CANCEL_FAILED,0);
                        throw new ScTransactionException("Execute branch rollback error,branchId:" + scResponseMessage.getBranchId()+",businessId:"+ transBranchInfo.getBusinessId());
                    }
                    break;
                default:
                    LOGGER.error("Execute branch transaction error: illegal response message: {} {}", transBranchInfo, scResponseMessage);
                    throw new ScTransactionException("Execute branch transaction error: illegal response message,businessId:" + transBranchInfo.getBusinessId());
            }
    }


    private String getParamKey(String branchName, String key) {
        return branchName + ":" + key;
    }

    public ScResponseMessage callBranchActuator(String url,ScRequestMessage scRequestMessage) throws Exception {
        ScResponseMessage scResponseMessage = callService.call(url, scRequestMessage);
        return scResponseMessage;
    }

    private void prepareInParallel(TransactionInfo transactionInfo){
        CountDownLatch countDownLatch=new CountDownLatch(transactionInfo.getBranchTransactionList().size());
        Map<String,Boolean> resultMap=new HashMap<>();
        MessageType messageType=MessageType.TYPE_BRANCH_PREPARE;
        for (TransBranchInfo branchTransaction : transactionInfo.getBranchTransactionList()) {
            ScRequestMessage scRequestMessage = RpcRequestBuilder.buildBranchRequest(branchTransaction,messageType);
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run(){
                    try {
                        ScResponseMessage scResponseMessage = null;
                        if(TransMode.TCC==TransMode.valueOf(branchTransaction.getTransMode())) {
                             scResponseMessage = callBranchActuator(branchTransaction.getUrlMap().get(ServerConstants.HttpAction.TCC_PREPARE_NAME), scRequestMessage);
                        }
                        else if(TransMode.XA==TransMode.valueOf(branchTransaction.getTransMode())){
                            scResponseMessage = callBranchActuator(branchTransaction.getUrlMap().get(ServerConstants.HttpAction.XA_PREPARE_NAME), scRequestMessage);
                        }
                        if(scResponseMessage.getResultInfo().code== TransactionResponseCode.SUCCESS.getCode()){
                            resultMap.put(branchTransaction.getBranchName(),true);
                            publishBranchTransEvent(branchTransaction.getBid(),TransStatus.TRYING,TransStatus.TRY_SUCCEED,0);
                        }
                        else{
                            resultMap.put(branchTransaction.getBranchName(),false);
                            publishBranchTransEvent(branchTransaction.getBid(),TransStatus.TRYING,TransStatus.TRY_FAILED,0);
                        }
                    }catch (Exception e){
                        LOGGER.error("prepare call branch error:{}",transactionInfo,e);
                        publishBranchTransEvent(branchTransaction.getBid(),TransStatus.TRYING,TransStatus.TRY_FAILED,0);
                        resultMap.put(branchTransaction.getBranchName(),false);
                    }finally {
                         countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await(transactionInfo.getTimeout(), TimeUnit.MILLISECONDS);
        }catch (InterruptedException ie){
            LOGGER.warn("wait branch prepare error!",ie);
        }
        for (TransBranchInfo branchTransaction : transactionInfo.getBranchTransactionList()){
            if(!resultMap.get(branchTransaction.getBranchName())){
                throw new ScTransactionException(TransactionResponseCode.BRANCH_PREPARE_FAILED,"branch prepare failed:"+branchTransaction.getBranchName());
            }
        }
    }


    private void commitInParallel(TransactionInfo transactionInfo){
        CountDownLatch countDownLatch=new CountDownLatch(transactionInfo.getBranchTransactionList().size());
        Map<String,Boolean> resultMap=new HashMap<>();
        MessageType messageType=MessageType.TYPE_BRANCH_COMMIT;
        for (TransBranchInfo branchTransaction : transactionInfo.getBranchTransactionList()) {
            ScRequestMessage scRequestMessage = RpcRequestBuilder.buildBranchRequest(branchTransaction, messageType);
            threadPoolExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        ScResponseMessage scResponseMessage = null;
                        if(TransMode.TCC==TransMode.valueOf(branchTransaction.getTransMode())) {
                            scResponseMessage = callBranchActuator(branchTransaction.getUrlMap().get(ServerConstants.HttpAction.TCC_COMMIT_NAME), scRequestMessage);
                        }
                        else if(TransMode.XA==TransMode.valueOf(branchTransaction.getTransMode())){
                            scResponseMessage = callBranchActuator(branchTransaction.getUrlMap().get(ServerConstants.HttpAction.XA_COMMIT_NAME), scRequestMessage);
                        }
                        if (scResponseMessage.getResultInfo().code == TransactionResponseCode.SUCCESS.getCode()) {
                            resultMap.put(branchTransaction.getBranchName(), true);
                            publishBranchTransEvent(branchTransaction.getBid(),null,TransStatus.COMMIT_SUCCEED,0);
                        } else {
                            resultMap.put(branchTransaction.getBranchName(), false);
                            publishBranchTransEvent(branchTransaction.getBid(),null,TransStatus.COMMIT_FAILED,0);
                        }
                    } catch (Exception e) {
                        resultMap.put(branchTransaction.getBranchName(), false);
                         publishBranchTransEvent(branchTransaction.getBid(),null,TransStatus.COMMIT_FAILED,0);
                        LOGGER.error("do commit in parallel error! {}", transactionInfo, e);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await(transactionInfo.getTimeout(), TimeUnit.MILLISECONDS);
        }catch (InterruptedException ie){
            LOGGER.warn("wait tcc branch commit error!",ie);
        }
        for (TransBranchInfo branchTransaction : transactionInfo.getBranchTransactionList()){
            if(!resultMap.get(branchTransaction.getBranchName())){
                throw new ScTransactionException(TransactionResponseCode.BRANCH_PREPARE_FAILED,"branch commit failed:"+branchTransaction.getBranchName());
            }
        }
    }


    private void rollbackInParallel(TransactionInfo transactionInfo){
        CountDownLatch countDownLatch=new CountDownLatch(transactionInfo.getBranchTransactionList().size());
        Map<String,Boolean> resultMap=new HashMap<>();
        MessageType messageType=MessageType.TYPE_BRANCH_COMMIT;
        for (TransBranchInfo branchTransaction : transactionInfo.getBranchTransactionList()) {
            ScRequestMessage scRequestMessage = RpcRequestBuilder.buildBranchRequest(branchTransaction, messageType);
            threadPoolExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        ScResponseMessage scResponseMessage = null;
                        if(TransMode.TCC==TransMode.valueOf(branchTransaction.getTransMode())) {
                            scResponseMessage = callBranchActuator(branchTransaction.getUrlMap().get(ServerConstants.HttpAction.TCC_ROLLBACK_NAME), scRequestMessage);
                        }
                        else if(TransMode.XA==TransMode.valueOf(branchTransaction.getTransMode())){
                            scResponseMessage = callBranchActuator(branchTransaction.getUrlMap().get(ServerConstants.HttpAction.XA_ROLLBACK_NAME), scRequestMessage);
                        }
                        if (scResponseMessage.getResultInfo().code == TransactionResponseCode.SUCCESS.getCode()) {
                            resultMap.put(branchTransaction.getBranchName(), true);
                            publishBranchTransEvent(branchTransaction.getBid(),null,TransStatus.CANCEL_SUCCEED,0);
                        } else {
                            resultMap.put(branchTransaction.getBranchName(), false);
                            publishBranchTransEvent(branchTransaction.getBid(),null,TransStatus.CANCEL_FAILED,0);
                        }
                    } catch (Exception e) {
                        LOGGER.error("do rollback in parallel error! {}", transactionInfo, e);
                        resultMap.put(branchTransaction.getBranchName(), false);
                        publishBranchTransEvent(branchTransaction.getBid(),null,TransStatus.CANCEL_FAILED,0);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await(transactionInfo.getTimeout(), TimeUnit.MILLISECONDS);
        }catch (InterruptedException ie){
            LOGGER.warn("wait tcc branch rollback error!",ie);
        }
        for (TransBranchInfo branchTransaction : transactionInfo.getBranchTransactionList()){
            if(!resultMap.get(branchTransaction.getBranchName())){
                throw new ScTransactionException(TransactionResponseCode.BRANCH_PREPARE_FAILED,"branch rollback failed:"+branchTransaction.getBranchName());
            }
        }
    }

    private TransCommonResponse processSaga(TransactionInfo transactionInfo) throws ScTransactionException{
        Map<String,Map<String,String>> returnParamMaps=new HashMap<>();
        ScTransRecord scTransRecord= TransInfoBuilder.transactionInfoToScTransRecord(transactionInfo);
        try {
            if (scTransRecord.getCallInParallel()==0) {
                TransStatus transStatus=null;
                boolean isAllSucceed=true;
                List<ScBranchRecord>  scBranchRecordList=new ArrayList<>();
                for(TransBranchInfo branchTransaction : transactionInfo.getBranchTransactionList()) {
                    try {
                        ScBranchRecord scBranchRecord = TransInfoBuilder.branchInfoToScBranchRecord(branchTransaction);
                        if(!scBranchRecordList.contains(scBranchRecord)) {
                            scBranchRecordList.add(scBranchRecord);
                        }
                        Map<String, Object> mapRequest = RpcRequestBuilder.buildSagaBranchMapRequest(branchTransaction, MessageType.TYPE_BRANCH_COMMIT);
                        transStatus = branchSagaTransCall(scBranchRecord, mapRequest, returnParamMaps);
                        if (transStatus != TransStatus.COMMIT_SUCCEED) {
                            isAllSucceed = false;
                            break;
                        }
                    }catch (Exception e){
                        LOGGER.warn("call saga branch  error! {}",branchTransaction,e);
                        isAllSucceed = false;
                        break;
                    }
                }
                publishGlobalTransEvent(scTransRecord.getTid(),null,isAllSucceed?TransStatus.COMMIT_SUCCEED:TransStatus.COMMIT_FAILED);
                if(isAllSucceed) {
                    return TransCommonResponse.builder().status(TransCommonResponse.Status.SUCCESS).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).build();
                }
                else{
                    boolean isAllRolledback=rollBackBranchRecords(scBranchRecordList);
                    if(isAllRolledback) {
                        publishGlobalTransEvent(scTransRecord.getTid(),null,TransStatus.CANCEL_SUCCEED);
                        return TransCommonResponse.builder().status(TransCommonResponse.Status.ROLLEDBACK).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).build();
                    }
                    else{
                        publishGlobalTransEvent(scTransRecord.getTid(),null,TransStatus.CANCEL_FAILED);
                        return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).build();
                    }
                }
            } else {
                CountDownLatch countDownLatch = new CountDownLatch(scTransRecord.getBranchTransactionList().size());
                Map<String,Boolean> resultMap=new HashMap<>();
                updateBranchRecords(scTransRecord.getBranchTransactionList(),TransStatus.READY.getValue());
                for (TransBranchInfo branchTransaction : transactionInfo.getBranchTransactionList()) {
                    Map<String,Object> mapRequest = RpcRequestBuilder.buildSagaBranchMapRequest(branchTransaction, MessageType.TYPE_BRANCH_COMMIT);
                    resultMap.put(branchTransaction.getBranchName(),false);
                    ScBranchRecord scBranchRecord = TransInfoBuilder.branchInfoToScBranchRecord(branchTransaction);
                    branchSagaSingleAsyncCall(scBranchRecord, resultMap, mapRequest, countDownLatch);
                }
                try {
                    countDownLatch.await(scTransRecord.getTimeout(), TimeUnit.MILLISECONDS);
                }catch (InterruptedException ie){
                    LOGGER.warn("wait saga branch execution finish error!",ie);
                }
                List<ScBranchRecord> succeedScBranchRecords=new ArrayList<>();
                List<ScBranchRecord> failedScBranchRecords=new ArrayList<>();
                for(ScBranchRecord scBranchRecord : scTransRecord.getBranchTransactionList()){
                    boolean isSucceed=resultMap.get(scBranchRecord.getBranchName());
                    if(isSucceed){
                        succeedScBranchRecords.add(scBranchRecord);
                    }
                    else {
                        failedScBranchRecords.add(scBranchRecord);
                    }
                }
                if(!succeedScBranchRecords.isEmpty()){
                    updateBranchRecords(succeedScBranchRecords,TransStatus.COMMIT_SUCCEED.getValue());
                }
                if(!failedScBranchRecords.isEmpty()){
                    updateBranchRecords(succeedScBranchRecords,TransStatus.COMMIT_FAILED.getValue());
                    boolean isAllRolledBack=rollBackBranchRecords(failedScBranchRecords);
                    if(isAllRolledBack){
                        publishGlobalTransEvent(scTransRecord.getTid(),null,failedScBranchRecords.isEmpty()?TransStatus.CANCEL_SUCCEED:TransStatus.COMMIT_FAILED);
                        return TransCommonResponse.builder().status(TransCommonResponse.Status.ROLLEDBACK).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).build();
                    }
                    else {
                        publishGlobalTransEvent(scTransRecord.getTid(),null,failedScBranchRecords.isEmpty()?TransStatus.COMMIT_FAILED:TransStatus.COMMIT_FAILED);
                        return TransCommonResponse.builder().status(TransCommonResponse.Status.FAILED).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).build();
                    }
                }
                publishGlobalTransEvent(scTransRecord.getTid(),null,failedScBranchRecords.isEmpty()?TransStatus.COMMIT_SUCCEED:TransStatus.COMMIT_FAILED);
            }
        }catch (Throwable e){
            LOGGER.error("execute branch transaction failed:{}",scTransRecord,e);
            throw new ScTransactionException(TransactionResponseCode.TRANSACTION_PROCESS_FAILED,e.getMessage());
        }
        return TransCommonResponse.builder().status(TransCommonResponse.Status.SUCCESS).branchId(transactionInfo.getBusinessId()).tid(transactionInfo.getTid()).build();
    }


    public void publishBranchTransEvent(long bid, TransStatus fromStatus, TransStatus toStatus, int retryCount){
        BranchTransEvent event = new BranchTransEvent();
        event.setBid(bid);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setModifyDate(new Date());
        event.setRetryCount(retryCount);
        tranTraceEventBus.post(event);
    }


    public void publishGlobalTransEvent(long tid, TransStatus fromStatus, TransStatus toStatus){
        GlobalTransEvent event = new GlobalTransEvent();
        event.setTid(tid);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setModifyDate(new Date());
        tranTraceEventBus.post(event);
    }


    private void updateBranchRecord(Long bid, int status, int retryCount) {
           Date now=new Date();
           branchTransService.updateStatusById(bid,status,retryCount,now);
    }


    private boolean rollBackBranchRecords(List<ScBranchRecord> branchRecords){
            boolean isAllSucceed = true;
            for(ScBranchRecord branchRecord:branchRecords){
                try{
                 if(TransStatus.CANCEL_SUCCEED!=TransStatus.getTransStatusByValue(branchRecord.getStatus())&&TransStatus.READY!=TransStatus.getTransStatusByValue(branchRecord.getStatus())) {
                     Map<String, Object> mapRequest = RpcRequestBuilder.buildSagaBranchMapRequest(branchRecord, MessageType.TYPE_BRANCH_ROLLBACK);
                     rollback(branchRecord, mapRequest);
                     publishBranchTransEvent(branchRecord.getBid(), null, TransStatus.CANCEL_SUCCEED, 0);
                 }
                }catch (Exception e){
                    LOGGER.warn("rollback saga transaction error:{}",branchRecord,e);
                    isAllSucceed=false;
                    publishBranchTransEvent(branchRecord.getBid(),null,TransStatus.CANCEL_FAILED,0);
                }
            }
            return isAllSucceed;
    }

    private void updateBranchRecords(List<ScBranchRecord> branchRecords,int status){
        branchTransService.updateStatusByBids(branchRecords.stream().map(e->e.getTid()).collect(Collectors.toList()),status,new Date());
    }

    private void updateScTransRecord(Long tid, int status, int retryCount) {
        globalTransService.updateStatusByTidAndStatus(tid,status,retryCount);
    }


    private void branchSagaSingleAsyncCall(ScBranchRecord scBranchRecord, Map<String,Boolean> resultMap, Map<String,Object> mapRequest, CountDownLatch countDownLatch){
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_COMMIT_NAME),mapRequest);
                    resultMap.put(scBranchRecord.getBranchName(),true);
                } catch (Exception e) {
                    if (scBranchRecord.getRetry() != null&&scBranchRecord.getRetry()!=0) {
                        LOGGER.error("branch call error:{}, try again!", scBranchRecord, e);
                        try {
                            callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_COMMIT_NAME),mapRequest);
                            resultMap.put(scBranchRecord.getBranchName(), true);
                        }catch(Exception ee){
                            resultMap.put(scBranchRecord.getBranchName(), false);
                            LOGGER.error("branch call again error:{}", scBranchRecord, ee);
                        }
                    }
                    else{
                        resultMap.put(scBranchRecord.getBranchName(), false);
                        LOGGER.error("branch call error:{}", scBranchRecord, e);
                    }
                } finally {
                    countDownLatch.countDown();
                }
            }
        });
    }


    public TransStatus branchSagaTransCall(ScBranchRecord scBranchRecord, Map<String,Object> mapRequest, Map<String,Map<String,String>> responseParamMaps) throws SQLException, ScServerException {
        Map<String,String> requestParams=new HashMap<>();
        makeRequestParam(responseParamMaps, scBranchRecord.getExternalParam(), requestParams);
        if(scBranchRecord.getBranchParam()!=null) {
            requestParams.putAll(JsonUtil.toMap(scBranchRecord.getBranchParam()));
        }
        mapRequest.put("requestParams",requestParams);
        return branchSingleSynCall(scBranchRecord,responseParamMaps, mapRequest);
    }


    private TransStatus branchSingleSynCall(ScBranchRecord scBranchRecord, Map<String,Map<String,String>> responseParamMaps, Map<String,Object> mapRequest){
        try{
            doCommit(scBranchRecord, responseParamMaps, mapRequest, 0);
            publishBranchTransEvent(scBranchRecord.getBid(), null, TransStatus.COMMIT_SUCCEED, 0);
        }catch (Exception e) {
            LOGGER.error("branch call error:{}, start to rollback", scBranchRecord, e);
            if(scBranchRecord.getRetry()!=null&&scBranchRecord.getRetry()!=0){
                try{
                    doCommit(scBranchRecord, responseParamMaps, mapRequest, 1);
                    publishBranchTransEvent(scBranchRecord.getBid(), null, TransStatus.COMMIT_SUCCEED, 1);
                }catch (Exception ee){
                    publishBranchTransEvent(scBranchRecord.getBid(), null, TransStatus.COMMIT_FAILED, 1);
                    return TransStatus.COMMIT_FAILED;
                }
            }
            else{
                publishBranchTransEvent(scBranchRecord.getBid(), null, TransStatus.COMMIT_FAILED, 0);
                return TransStatus.COMMIT_FAILED;
            }
        }
        return TransStatus.COMMIT_SUCCEED;
    }

    public void doCommit(ScBranchRecord scBranchRecord, Map<String, Map<String, String>> responseParamMaps, Map<String, Object> mapRequest, int i) {
        Map<String, String> returnParamMap = null;
        if(TransMode.TCC==TransMode.fromId(scBranchRecord.getTransMode())) {
            returnParamMap = callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.TCC_COMMIT_NAME), mapRequest);
        }
        else if(TransMode.XA==TransMode.fromId(scBranchRecord.getTransMode())){
            returnParamMap = callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.XA_COMMIT_NAME), mapRequest);
        }
        else if(TransMode.SAGA==TransMode.fromId(scBranchRecord.getTransMode())){
            returnParamMap = callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_COMMIT_NAME), mapRequest);
        }
        if (returnParamMap != null) {
            responseParamMaps.put(scBranchRecord.getBranchName(), returnParamMap);
        }
        //publishBranchTransEvent(scBranchRecord.getBid(), null, TransStatus.COMMIT_SUCCEED, i);
    }

    private TransStatus doRollback(ScBranchRecord scBranchRecord, Map<String, Object> mapRequest) {
        try {
            rollback(scBranchRecord, mapRequest);
            publishBranchTransEvent(scBranchRecord.getBid(),null,TransStatus.CANCEL_SUCCEED,0);
            return TransStatus.CANCEL_SUCCEED;
        } catch (Exception se) {
            LOGGER.error("rollback failed, shift to async rollback:{} ", scBranchRecord, se);
            publishBranchTransEvent(scBranchRecord.getBid(),null,TransStatus.CANCEL_FAILED,0);
            return TransStatus.CANCEL_SUCCEED;
        }
    }

    private void rollback(ScBranchRecord scBranchRecord,Map<String,Object> mapRequest) throws ScServerException{
        if(TransMode.TCC==TransMode.fromId(scBranchRecord.getTransMode())) {
             callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.TCC_ROLLBACK_NAME), mapRequest);
        }
        else if(TransMode.XA==TransMode.fromId(scBranchRecord.getTransMode())){
            callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.XA_ROLLBACK_NAME), mapRequest);
        }
        else if(TransMode.SAGA==TransMode.fromId(scBranchRecord.getTransMode())){
            callService.call(scBranchRecord.getUrlMap().get(ServerConstants.HttpAction.SAGA_ROLLBACK_NAME), mapRequest);
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

}
