package com.ops.sc.core.gather;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.ops.sc.common.bean.FailureTransInfoRequestParams;
import com.ops.sc.common.dto.admin.TransInfoDTO;
import com.ops.sc.common.enums.CallbackStrategy;
import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.model.TransBranchInfo;
import com.ops.sc.common.utils.DateUtil;
import com.ops.sc.common.utils.DistributeIdGenerator;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.common.bean.Page;
import com.ops.sc.common.bean.TransInfoQueryParams;
import com.ops.sc.common.model.TransactionInfo;
import com.ops.sc.common.bean.TransInfoRequestQueryParams;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.GlobalSagaTransRequest;
import com.ops.sc.rpc.dto.GlobalTransRequest;
import org.springframework.util.CollectionUtils;

import com.ops.sc.common.enums.TransStatus;



public class TransInfoBuilder {

    public static ScTransRecord buildGlobalTransInfo(long tid, final GlobalTransRequest request) {
        ScTransRecord scTransRecord = new ScTransRecord();
        scTransRecord.setTid(tid);
        scTransRecord.setGroupId(request.getTransGroupId());
        scTransRecord.setBusinessId(request.getBusinessId());
        TransMode transMode= TransMode.valueOf(request.getTransMode());
        scTransRecord.setTransMode(request.getTransMode());
        if(transMode==TransMode.TCC){
            scTransRecord.setStatus(TransStatus.READY.getValue());
        }
        else {
            scTransRecord.setStatus(TransStatus.TRYING.getValue());
        }
        scTransRecord.setCallerIp(request.getCallerIp());
        scTransRecord.setTimeout(request.getTimeout().getValue());
        scTransRecord.setTimeoutType(request.getTimeoutType().getValue());
        scTransRecord.setAppName(request.getAppName());
        scTransRecord.setCallbackStrategy(request.getCallbackStrategy().getValue());
        scTransRecord.setRetryCount(0);
        scTransRecord.setDataSource(request.getDataSource());
        scTransRecord.setCallInParallel(request.getCallInParallel().getValue());
        scTransRecord.setRollBackInfo(request.getRollbackInfo());
        scTransRecord.setBranchTransactionList(new ArrayList<>());
        if(request.getBranchTransDetailsList()!=null){
            Map<String,ScBranchRecord> sortedBranchMap= Maps.newTreeMap();
            for(int i=0;i<request.getBranchTransDetailsList().size();i++){
                GlobalTransRequest.BranchTransDetail branchTransDetail=request.getBranchTransDetailsList().get(i);
                sortedBranchMap.put(branchTransDetail.getBranchTransName(),getScBranchInfo(tid,transMode,request.getCallerIp(),request.getBusinessId(),branchTransDetail,i+1));
            }
            scTransRecord.getBranchTransactionList().addAll(sortedBranchMap.values());
        }
        return scTransRecord;
    }


    public static ScTransRecord buildGlobalSagaTransInfo(long tid, final GlobalSagaTransRequest request) {
        ScTransRecord scTransRecord = new ScTransRecord();
        scTransRecord.setTid(tid);
        scTransRecord.setGroupId(request.getTransGroupId());
        scTransRecord.setBusinessId(request.getBusinessId());
        //transactionInfo.setCheckBackMetaId(request.getCheckBackMetaId().getValue());
        TransMode transMode= TransMode.valueOf(request.getTransMode());
        scTransRecord.setTransMode(request.getTransMode());
        if(transMode==TransMode.TCC){
            scTransRecord.setStatus(TransStatus.READY.getValue());
        }
        else {
            scTransRecord.setStatus(TransStatus.TRYING.getValue());
        }
        scTransRecord.setCallerIp(request.getCallerIp());
        scTransRecord.setTimeout(request.getTimeout().getValue());
        scTransRecord.setTimeoutType(request.getTimeoutType().getValue());
        scTransRecord.setAppName(request.getAppName());
        //transactionInfo.setTransName(request.getTransName());
        //transactionInfo.setInstanceName(request.getInstanceName());
        scTransRecord.setBranchTransactionList(new ArrayList<>());
        if(request.getBranchTransDetailsList()!=null){
            Map<String,ScBranchRecord> sortedBranchMap= Maps.newTreeMap();
            for(int i=0;i<request.getBranchTransDetailsList().size();i++){
                GlobalSagaTransRequest.BranchSagaTransDetail branchTransDetail = request.getBranchTransDetailsList().get(i);
                sortedBranchMap.put(branchTransDetail.getBranchName(),getScBranchInfo(request.getBusinessId(),tid, branchTransDetail,i));
            }
            scTransRecord.getBranchTransactionList().addAll(sortedBranchMap.values());
        }
        return scTransRecord;
    }



    public static TransactionInfo scTransRecordToTransactionInfo(ScTransRecord scTransRecord){
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setTid(scTransRecord.getTid());
        transactionInfo.setGroupId(scTransRecord.getGroupId());
        transactionInfo.setBusinessId(scTransRecord.getBusinessId());
        //transactionInfo.setCheckBackMetaId(request.getCheckBackMetaId().getValue());
        TransMode transMode= TransMode.valueOf(scTransRecord.getTransMode());
        transactionInfo.setTransMode(transMode);
        if(transMode==TransMode.TCC){
            transactionInfo.setStatus(TransStatus.READY.getValue());
        }
        else {
            transactionInfo.setStatus(TransStatus.TRYING.getValue());
        }
        transactionInfo.setCallerIp(scTransRecord.getCallerIp());
        transactionInfo.setTimeout(scTransRecord.getTimeout());
        transactionInfo.setTimeoutType(scTransRecord.getTimeoutType());
        transactionInfo.setAppName(scTransRecord.getAppName());
        //transactionInfo.setTransName(request.getTransName());
        //transactionInfo.setInstanceName(request.getInstanceName());
        transactionInfo.setCallbackStrategy(scTransRecord.getCallbackStrategy());
        transactionInfo.setRetryCount(scTransRecord.getRetryCount());
        transactionInfo.setDataSource(scTransRecord.getDataSource());
        transactionInfo.setBranchTransactionList(new ArrayList<>());
        for(ScBranchRecord branchTransaction:scTransRecord.getBranchTransactionList()){
            transactionInfo.getBranchTransactionList().add(scBranchTransactionToBranchTransaction(branchTransaction,transMode.name()));
        }
        return transactionInfo;
    }

    public static TransBranchInfo scBranchTransactionToBranchTransaction(ScBranchRecord scBranchRecord,String transMode){
        TransBranchInfo branchTransaction=new TransBranchInfo();
        branchTransaction.setBranchName(scBranchRecord.getBranchName());
        branchTransaction.setTid(scBranchRecord.getTid());
        branchTransaction.setBusinessId(scBranchRecord.getBusinessId());
        branchTransaction.setBranchTransName(scBranchRecord.getBranchTransName());
        if(scBranchRecord.getBranchParam()!=null) {
            branchTransaction.setParamMap(JsonUtil.toMap(scBranchRecord.getBranchParam()));
        }
        if(scBranchRecord.getExternalParam()!=null) {
            branchTransaction.setExternalMap(JsonUtil.toMap(scBranchRecord.getExternalParam()));
        }
        if(scBranchRecord.getRollbackParam()!=null){
            branchTransaction.setRollbackParamMap(JsonUtil.toMap(scBranchRecord.getRollbackParam()));
        }
        if(scBranchRecord.getReturnParam()!=null){
            branchTransaction.setReturnParamList(JsonUtil.toObject(List.class,scBranchRecord.getReturnParam()));
        }
        branchTransaction.setTransMode(transMode);
        branchTransaction.setParentName(scBranchRecord.getParentName());
        branchTransaction.setBid(scBranchRecord.getBid());
        branchTransaction.setCallerIp(scBranchRecord.getCallerIp());
        branchTransaction.setCreateTime(scBranchRecord.getCreateTime());
        branchTransaction.setDataSource(scBranchRecord.getDataSource());
        branchTransaction.setModifyTime(scBranchRecord.getModifyTime());
        branchTransaction.setEndTime(scBranchRecord.getEndTime());
        branchTransaction.setRetryCount(scBranchRecord.getRetryCount());
        branchTransaction.setStatus(scBranchRecord.getStatus());
        branchTransaction.setTimeout(scBranchRecord.getTimeout());
        branchTransaction.setTimeoutType(scBranchRecord.getTimeoutType());
        branchTransaction.setTransactionName(scBranchRecord.getTransactionName());
        branchTransaction.setUrlMap(scBranchRecord.getUrlMap());
        branchTransaction.setOrderNo(scBranchRecord.getOrderNo());
        return branchTransaction;
    }


    public static ScBranchRecord branchInfoToScBranchRecord(TransBranchInfo branchTransaction){
        ScBranchRecord scBranchRecord=new ScBranchRecord();
        scBranchRecord.setBranchName(branchTransaction.getBranchName());
        scBranchRecord.setTid(branchTransaction.getTid());
        scBranchRecord.setBusinessId(branchTransaction.getBusinessId());
        scBranchRecord.setBranchTransName(branchTransaction.getBranchTransName());
        if(!CollectionUtils.isEmpty(branchTransaction.getParamMap())) {
            scBranchRecord.setBranchParam(JsonUtil.toString(branchTransaction.getParamMap()));
        }
        if(!CollectionUtils.isEmpty(branchTransaction.getExternalMap())) {
            scBranchRecord.setExternalParam(JsonUtil.toString(branchTransaction.getExternalMap()));
        }
        if(!CollectionUtils.isEmpty(branchTransaction.getReturnParamList())){
            scBranchRecord.setReturnParam(JsonUtil.toString(branchTransaction.getReturnParamList()));
        }
        scBranchRecord.setParentName(branchTransaction.getParentName());
        scBranchRecord.setBid(branchTransaction.getBid());
        scBranchRecord.setTransMode(TransMode.valueOf(branchTransaction.getTransMode()).getValue());
        scBranchRecord.setCallerIp(branchTransaction.getCallerIp());
        scBranchRecord.setCreateTime(branchTransaction.getCreateTime());
        scBranchRecord.setDataSource(branchTransaction.getDataSource());
        scBranchRecord.setModifyTime(branchTransaction.getModifyTime());
        scBranchRecord.setEndTime(branchTransaction.getEndTime());
        scBranchRecord.setRetryCount(branchTransaction.getRetryCount());
        scBranchRecord.setStatus(branchTransaction.getStatus());
        scBranchRecord.setTimeout(branchTransaction.getTimeout());
        scBranchRecord.setTimeoutType(branchTransaction.getTimeoutType());
        scBranchRecord.setTransactionName(branchTransaction.getTransactionName());
        scBranchRecord.setUrlMap(branchTransaction.getUrlMap());
        scBranchRecord.setOrderNo(branchTransaction.getOrderNo());
        scBranchRecord.setRetry(branchTransaction.getRetry());
        return scBranchRecord;
    }

    public static ScTransRecord transactionInfoToScTransRecord(TransactionInfo transactionInfo){
        ScTransRecord scTransRecord = new ScTransRecord();
        scTransRecord.setTid(transactionInfo.getTid());
        scTransRecord.setGroupId(transactionInfo.getGroupId());
        scTransRecord.setBusinessId(transactionInfo.getBusinessId());
        //transactionInfo.setCheckBackMetaId(request.getCheckBackMetaId().getValue());
        TransMode transMode= transactionInfo.getTransMode();
        scTransRecord.setTransMode(transMode.name());
        scTransRecord.setStatus(TransStatus.READY.getValue());
        scTransRecord.setCallerIp(transactionInfo.getCallerIp());
        scTransRecord.setTimeout(transactionInfo.getTimeout());
        scTransRecord.setTimeoutType(transactionInfo.getTimeoutType());
        scTransRecord.setAppName(transactionInfo.getAppName());
        //transactionInfo.setTransName(request.getTransName());
        scTransRecord.setCallbackStrategy(transactionInfo.getCallbackStrategy());
        Date date=new Date();
        scTransRecord.setCreateTime(date);
        scTransRecord.setModifyTime(date);
        scTransRecord.setRetryCount(transactionInfo.getRetryCount());
        scTransRecord.setDataSource(transactionInfo.getDataSource());
        scTransRecord.setBranchTransactionList(new ArrayList<>());
        for(int i=0;i<transactionInfo.getBranchTransactionList().size();i++){
            TransBranchInfo branchTransaction=transactionInfo.getBranchTransactionList().get(i);
            scTransRecord.getBranchTransactionList().add(branchInfoToScBranchRecord(branchTransaction));
        }
        return scTransRecord;
    }


    public static ScTransRecord globalSagaToScTransRecord(long tid,GlobalSagaTransRequest globalSagaTransRequest){
        ScTransRecord scTransRecord=new ScTransRecord();
        scTransRecord.setTid(tid);
        scTransRecord.setGroupId(globalSagaTransRequest.getTransGroupId());
        scTransRecord.setBusinessId(globalSagaTransRequest.getBusinessId());
        TransMode transMode= TransMode.valueOf(globalSagaTransRequest.getTransMode());
        scTransRecord.setTransMode(transMode.name());
        scTransRecord.setStatus(TransStatus.READY.getValue());
        scTransRecord.setRetryCount(0);
        scTransRecord.setCallerIp(globalSagaTransRequest.getCallerIp());
        scTransRecord.setTimeout(globalSagaTransRequest.getTimeout().getValue());
        scTransRecord.setTimeoutType(globalSagaTransRequest.getTimeoutType().getValue());
        scTransRecord.setAppName(globalSagaTransRequest.getAppName());
        Date date=new Date();
        scTransRecord.setCreateTime(date);
        scTransRecord.setModifyTime(date);
        scTransRecord.setEventType(globalSagaTransRequest.getTransType());
        scTransRecord.setBranchTransactionList(new ArrayList<>());
        scTransRecord.setCallbackStrategy(CallbackStrategy.IN_ORDER.getValue());
        if(globalSagaTransRequest.getBranchTransDetailsList()!=null){
            for (int i=0;i<globalSagaTransRequest.getBranchTransDetailsList().size();i++){
                GlobalSagaTransRequest.BranchSagaTransDetail branchSagaTransDetail=globalSagaTransRequest.getBranchTransDetailsList().get(i);
                ScBranchRecord transBranchInfo = getScBranchInfo(globalSagaTransRequest.getBusinessId(),tid,branchSagaTransDetail,i);
                scTransRecord.getBranchTransactionList().add(transBranchInfo);
            }
        }
        return scTransRecord;
    }



    public static ScBranchRecord getScBranchInfo(String businessId, long tid, GlobalSagaTransRequest.BranchSagaTransDetail branchTransDetail,int order) {
        ScBranchRecord scBranchRecord=new ScBranchRecord();
        scBranchRecord.setBusinessId(businessId);
        scBranchRecord.setTid(tid);
        scBranchRecord.setBranchTransName(branchTransDetail.getBranchTransName());
        scBranchRecord.setBranchName(branchTransDetail.getBranchName());
        scBranchRecord.setTimeoutType(branchTransDetail.getTimeoutType().getValue());
        scBranchRecord.setTransactionName(branchTransDetail.getBranchName());
        scBranchRecord.setTimeout(branchTransDetail.getTimeout().getValue());
        scBranchRecord.setRetry(branchTransDetail.getRetryRequired()?1:0);
        scBranchRecord.setRetryCount(0);
        scBranchRecord.setTransMode(TransMode.SAGA.getValue());
        Date date=new Date();
        scBranchRecord.setCreateTime(date);
        scBranchRecord.setModifyTime(date);
        if(!CollectionUtils.isEmpty(branchTransDetail.getReturnParamList())) {
            scBranchRecord.setReturnParam(JsonUtil.toString(branchTransDetail.getReturnParamList()));
        }
        if(!CollectionUtils.isEmpty(branchTransDetail.getBranchParamMap())) {
            scBranchRecord.setBranchParam(JsonUtil.toString(branchTransDetail.getBranchParamMap()));
        }
        scBranchRecord.setRollbackParam(JsonUtil.toString(branchTransDetail.getBranchRollbackParamMap()));
        if(!CollectionUtils.isEmpty(branchTransDetail.getExternalParamMap())) {
            scBranchRecord.setExternalParam(JsonUtil.toString(branchTransDetail.getExternalParamMap()));
        }
        scBranchRecord.setStatus(TransStatus.TRYING.getValue());
        if(!CollectionUtils.isEmpty(branchTransDetail.getParentBranchNamesList())) {
            scBranchRecord.setParentName(String.join(",", branchTransDetail.getParentBranchNamesList()));
        }
        scBranchRecord.setHasParent(branchTransDetail.getHasParent()?1:0);
        scBranchRecord.setBid(DistributeIdGenerator.generateId());
        scBranchRecord.setOrderNo(order);
        return scBranchRecord;
    }


    public static ScBranchRecord getScBranchInfo(Long tid,TransMode transMode,String callerIp,String businessId, GlobalTransRequest.BranchTransDetail branchTransDetail,int order) {
        ScBranchRecord scBranchRecord=new ScBranchRecord();
        scBranchRecord.setTid(tid);
        scBranchRecord.setBusinessId(businessId);
        scBranchRecord.setBranchTransName(branchTransDetail.getBranchTransName());
        scBranchRecord.setBranchName(branchTransDetail.getBranchName());
        scBranchRecord.setTransactionName(branchTransDetail.getTransactionName());
        scBranchRecord.setTimeoutType(branchTransDetail.getTimeOutType().getValue());
        if(!CollectionUtils.isEmpty(branchTransDetail.getBranchParamMap())) {
            scBranchRecord.setBranchParam(JsonUtil.toString(branchTransDetail.getBranchParamMap()));
        }
        if(!CollectionUtils.isEmpty(branchTransDetail.getExternalParamMap())) {
            scBranchRecord.setExternalParam(JsonUtil.toString(branchTransDetail.getExternalParamMap()));
        }
        if(!CollectionUtils.isEmpty(branchTransDetail.getReturnParamList())){
            scBranchRecord.setReturnParam(JsonUtil.toString(branchTransDetail.getReturnParamList()));
        }
        if(!CollectionUtils.isEmpty(branchTransDetail.getRollbackParamMap())){
            scBranchRecord.setRollbackParam(JsonUtil.toString(branchTransDetail.getRollbackParamMap()));
        }
        scBranchRecord.setRetry(branchTransDetail.getRetryRequired()?1:0);
        scBranchRecord.setRetryCount(0);
        scBranchRecord.setTransMode(transMode.getValue());
        scBranchRecord.setCallerIp(callerIp);
        Date date=new Date();
        scBranchRecord.setCreateTime(date);
        scBranchRecord.setModifyTime(date);
        scBranchRecord.setStatus(TransStatus.TRYING.getValue());
        if(!CollectionUtils.isEmpty(branchTransDetail.getParentBranchNamesList())) {
            scBranchRecord.setParentName(JsonUtil.toString(branchTransDetail.getParentBranchNamesList()));
        }
        scBranchRecord.setHasParent(branchTransDetail.getHasParent()?1:0);
        scBranchRecord.setBid(DistributeIdGenerator.generateId());
        scBranchRecord.setOrderNo(order);
        return scBranchRecord;
    }


    private static TransBranchInfo getTransBranchInfo(long tid, GlobalTransRequest.BranchTransDetail branchTransDetail) {
        TransBranchInfo branchTransaction=new TransBranchInfo();
        branchTransaction.setTid(tid);
        branchTransaction.setBranchTransName(branchTransDetail.getBranchTransName());
        branchTransaction.setBranchName(branchTransDetail.getBranchName());
        branchTransaction.setTransactionName(branchTransDetail.getTransactionName());
        branchTransaction.setTimeoutType(branchTransDetail.getTimeOutType().getValue());
        branchTransaction.setParamMap(branchTransDetail.getBranchParamMap());
        branchTransaction.setExternalMap(branchTransDetail.getExternalParamMap());
        branchTransaction.setStatus(TransStatus.TRYING.getValue());
        if(!CollectionUtils.isEmpty(branchTransDetail.getParentBranchNamesList())) {
            branchTransaction.setParentName(JsonUtil.toString(branchTransDetail.getParentBranchNamesList()));
        }
        branchTransaction.setHasParent(branchTransDetail.getHasParent());
        branchTransaction.setBid(DistributeIdGenerator.generateId());
        return branchTransaction;
    }

    public static ScBranchRecord getScBranchInfo(long tid,long bid, BranchTransRequest branchTransDetail) {
        ScBranchRecord branchTransaction=new ScBranchRecord();
        branchTransaction.setTid(tid);
        branchTransaction.setBranchTransName(branchTransDetail.getBranchTransName());
        //branchTransaction.setTransactionName(branchTransDetail.getTransactionName());
        branchTransaction.setBusinessId(branchTransDetail.getBusinessId());
        branchTransaction.setBid(bid);
        Date now=new Date();
        branchTransaction.setCreateTime(now);
        branchTransaction.setModifyTime(now);
        branchTransaction.setCallerIp(branchTransDetail.getCallerIp());
        branchTransaction.setTransMode(branchTransDetail.getBranchType().getValue());
        //branchTransaction.setRetryCount(branchTransDetail.get);
       /* if(branchTransDetail.getParams()!=null) {
            branchTransaction.setBranchParam(new String(branchTransDetail.getParams().toByteArray(), Charset.defaultCharset()));
        }
        if(branchTransDetail.getExternalParams()!=null) {
            branchTransaction.setExternalParam(new String(branchTransDetail.getExternalParams().toByteArray(), Charset.defaultCharset()));
        }
        if(branchTransDetail.getReturnParams()!=null) {
            branchTransaction.setReturnParam(new String(branchTransDetail.getReturnParams().toByteArray(), Charset.defaultCharset()));
        }
        if(branchTransDetail.getRollbackParams()!=null) {
            branchTransaction.setRollbackParam(new String(branchTransDetail.getRollbackParams().toByteArray(), Charset.defaultCharset()));
        }
        branchTransaction.setRetryCount(0);
        branchTransaction.setRetry(branchTransDetail.getRetry()?1:0);
        branchTransaction.setTimeout(branchTransDetail.getTimeout().getValue());
        branchTransaction.setTimeoutType(branchTransDetail.getTimeoutStrategy().getValue());
        branchTransaction.setTransactionName(branchTransDetail.getTransactionName());
        branchTransaction.setTimeoutStrategy(branchTransDetail.getTimeOutType().getValue());
        branchTransaction.setBranchParam(JsonUtil.toString(branchTransDetail.getBranchParamMapMap()));
        if(branchTransDetail.getExternalParamMap()!=null) {
            Map<String,List<String>> externalMap=new HashMap<>();
            for(String key:branchTransDetail.getExternalParamMap().keySet()) {
                externalMap.put(key,branchTransDetail.getExternalParamMap().get(key).getValueList());
            }
            branchTransaction.setExternalParam(JsonUtil.toString(externalMap));
        }

        branchTransaction.setStatus(TransStatus.TRYING.getValue());
        branchTransaction.setParentName(branchTransDetail.getParentBranchName());
        branchTransaction.setHasParent(branchTransDetail.getHasParent()?1:0);
        branchTransaction.setBid(DistributeIdGenerator.generateId());*/
        return branchTransaction;
    }


    public static TransInfoQueryParams buildTccTransInfoQueryParams(List<String> groupIdList,
                                                                    TransInfoRequestQueryParams requestQueryParams) throws IllegalArgumentException {
        if (CollectionUtils.isEmpty(groupIdList)) {
            throw new IllegalArgumentException("groupIdList is empty");
        }
        TransInfoQueryParams transInfoQueryParams = new TransInfoQueryParams();
        transInfoQueryParams.setGroupIdList(groupIdList);

        if (requestQueryParams.getTid()!=null) {
            transInfoQueryParams.setTid(requestQueryParams.getTid());
            return transInfoQueryParams;
        }
        transInfoQueryParams.setTransName(requestQueryParams.getTransName());
        transInfoQueryParams.setCreateTimeStart(requestQueryParams.getSearchTimeStart());
        transInfoQueryParams.setCreateTimeEnd(requestQueryParams.getSearchTimeEnd());
        transInfoQueryParams.setBizId(requestQueryParams.getBizId());
        transInfoQueryParams.setPage(new Page(requestQueryParams.getLimit(), requestQueryParams.getOffset()));
        transInfoQueryParams.setStatusList(handleRequestQueryParamStatus(requestQueryParams));
        transInfoQueryParams.setAppName(requestQueryParams.getAppName());
        return transInfoQueryParams;
    }

    public static TransInfoQueryParams buildParams(List<String> groupIdList, List<Integer> abnormalStatus,
                                                   FailureTransInfoRequestParams params) {
        TransInfoQueryParams transInfoQueryParams = new TransInfoQueryParams();
        transInfoQueryParams.setGroupIdList(groupIdList);
        transInfoQueryParams.setCreateTimeEnd(params.getSearchTimeEnd());
        transInfoQueryParams.setCreateTimeStart(params.getSearchTimeStart());
        transInfoQueryParams.setPage(new Page(params.getLimit(), 0));
        transInfoQueryParams.setStatusList(abnormalStatus);
        transInfoQueryParams.setAppName(params.getAppName());
        return transInfoQueryParams;
    }

    /**
     * 数据库获取transInfo变量转变为TransInfoDTO
     *
     * @param transactionInfo
     * @return
     */
    public static TransInfoDTO buildTransDTO(ScTransRecord transactionInfo) {
        TransInfoDTO transInfoDTO = new TransInfoDTO();
        transInfoDTO.setId(transactionInfo.getTid());
        transInfoDTO.setTid(transactionInfo.getTid());
        transInfoDTO.setBizId(transactionInfo.getBusinessId());
        transInfoDTO.setStatus(transactionInfo.getStatus());
        transInfoDTO.setTransGroupName(transactionInfo.getGroupId());
        transInfoDTO.setCallerIp(transactionInfo.getCallerIp());
        //transInfoDTO.setInstanceName(transactionInfo.getInstanceName());
        transInfoDTO.setCreateTime(DateUtil.date2String(transactionInfo.getCreateTime()));
        //transInfoDTO.setTransName(transactionInfo.getTransName());
        transInfoDTO.setAppName(transactionInfo.getAppName());
        Date endTime = transactionInfo.getEndTime();
        if (endTime != null) {
            transInfoDTO.setEndTime(DateUtil.date2String(transactionInfo.getEndTime()));
        }
        return transInfoDTO;
    }


    public static List<Integer> handleRequestQueryParamStatus(TransInfoRequestQueryParams requestQueryParams)
            throws IllegalArgumentException {
        if (requestQueryParams.getActiveTrans()) {
            List<Integer> activeStatusList = getNormalTransStatusList();
            if (requestQueryParams.getStatus() == null) {
                return activeStatusList;
            } else {
                if (!activeStatusList.contains(requestQueryParams.getStatus())) {
                    // activeStatusList 和 query.status没有交集
                    throw new IllegalArgumentException();
                }
            }
        }
        return requestQueryParams.getStatus() == null ? new ArrayList<>()
                : Collections.singletonList(requestQueryParams.getStatus());
    }

    public static List<Integer> getNormalTransStatusList() {
        List<TransStatus> normalTransStatusList = new ArrayList<>();
        normalTransStatusList.add(TransStatus.READY);
        normalTransStatusList.add(TransStatus.TRY_SUCCEED);
        normalTransStatusList.add(TransStatus.COMMIT_SUCCEED);
        normalTransStatusList.add(TransStatus.TRYING);
        return normalTransStatusList.stream().map(TransStatus::getValue).collect(Collectors.toList());
    }


    public static List<Integer> getFailedTransStatusList() {
        List<TransStatus> failedTransStatusList = new ArrayList<>();
        failedTransStatusList.add(TransStatus.COMMIT_FAILED);
        failedTransStatusList.add(TransStatus.TRY_FAILED);
        failedTransStatusList.add(TransStatus.CANCEL_FAILED);
        failedTransStatusList.add(TransStatus.CANCEL_SUCCEED);
        failedTransStatusList.add(TransStatus.TRY_TIMEOUT);
        return failedTransStatusList.stream().map(TransStatus::getValue).collect(Collectors.toList());
    }

}
