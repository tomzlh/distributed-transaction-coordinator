package com.ops.sc.compensator.service.impl;

import com.ops.sc.common.bean.ResultCode;
import com.ops.sc.common.bean.TransInfoQueryParams;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.constant.ServerConstants;
import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.compensator.grpc.sync.CompensatorGrpcSyncClient;
import com.ops.sc.compensator.service.AlarmService;
import com.ops.sc.compensator.service.TransactionProcessor;
import com.ops.sc.core.build.RpcRequestBuilder;
import com.ops.sc.compensator.dao.TransBranchInfoDao;
import com.ops.sc.compensator.dao.TransInfoDao;
import com.ops.sc.compensator.service.TransactionOperationService;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.BranchTransResponse;
import com.ops.sc.rpc.dto.TransCompensationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;



@Service
public class TransactionOperationServiceImpl implements TransactionOperationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionOperationServiceImpl.class);

    @Autowired
    private TransInfoDao transInfoDao;

    @Autowired
    private TransBranchInfoDao transBranchInfoDao;


    @Autowired
    private TransactionProcessor transactionProcessor;

    @Resource
    private AlarmService alarmService;



    @Override
    public List<ScTransRecord> findByStatus(List<Integer> statusList) {
        List<ScTransRecord> scTransRecordList = transInfoDao.findByStatus(statusList);
        if(!CollectionUtils.isEmpty(scTransRecordList)){
            for(ScTransRecord scTransRecord:scTransRecordList) {
                List<ScBranchRecord> scBranchRecordList = transBranchInfoDao.findByTid(scTransRecord.getTid());
                Collections.sort(scBranchRecordList, new Comparator<ScBranchRecord>() {
                    @Override
                    public int compare(ScBranchRecord o1, ScBranchRecord o2) {
                        return o1.getOrderNo()-o2.getOrderNo()>=0?-1:1;
                    }
                });
                scTransRecord.setBranchTransactionList(scBranchRecordList);
            }
        }
        return scTransRecordList;
    }

    @Override
    public List<ScTransRecord> find(TransInfoQueryParams queryParams) {
        List<ScTransRecord> scTransRecordList = transInfoDao.findByConditions(queryParams);
        return scTransRecordList;
    }

    @Override
    public int updateStatusByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus,int retryCount) {
        return transInfoDao.updateStatusByTidAndStatus(tid, fromStatus, toStatus,retryCount);
    }

    @Override
    public int updateStatusByBidAndStatus(Long bid, Integer toStatus, int retryCount) {
        return transBranchInfoDao.updateStatusByBranchId(bid,retryCount,toStatus);
    }


    @Override
    public Boolean isGlobalTransTryTimeout(ScTransRecord scTransRecord) {
        Date createTime = scTransRecord.getCreateTime();
        Date now = new Date();
        return (now.getTime() - createTime.getTime()) > scTransRecord.getTimeout();
    }



    @Override
    public void checkAbnormalGlobalTrans(ScTransRecord scTransRecord, CompensatorGrpcSyncClient compensatorGrpcSyncClient)  throws RpcException{
        if (!isGlobalTransTryTimeout(scTransRecord)) {
            throw new IllegalStateException("UnSupport status when checkBack timeout global transaction");
        }
        LOGGER.info("Start to check abnormal global transaction, tid: {}", scTransRecord.getTid());
        TransStatus globalTransStatus =TransStatus.getTransStatusByValue(scTransRecord.getStatus());
        LOGGER.info("CheckGlobalTransaction global transaction status: {} tid: {} ", globalTransStatus, scTransRecord.getTid());

        if (scTransRecord.getRetryCount() >= ServerConstants.MAX_RETRY_TIMES) {
            LOGGER.info("Execute transaction exceed max times，set status to try timeout. tid: {}", scTransRecord.getTid());
            transInfoDao.updateStatusByTidAndStatus(scTransRecord.getTid(),null,
                    TransStatus.TRY_TIMEOUT.getValue(),scTransRecord.getRetryCount()+1);
        }
        TransCompensationRequest transCompensationRequest=RpcRequestBuilder.buildTransCompensationRequest(scTransRecord);
        transactionProcessor.compensate(transCompensationRequest,compensatorGrpcSyncClient);
    }



    public BranchTransResponse processBranchTransPrepare(ScBranchRecord scBranchRecord,CompensatorGrpcSyncClient compensatorGrpcSyncClient) throws RpcException {
        BranchTransRequest branchTransRequest = RpcRequestBuilder.buildBranchTransRequest(scBranchRecord.getBusinessId(), Constants.PREPARE, scBranchRecord);
        BranchTransResponse branchTransResponse=transactionProcessor.prepare(branchTransRequest,compensatorGrpcSyncClient);
        return branchTransResponse;
    }

    public BranchTransResponse processBranchTransCommit(ScBranchRecord scBranchRecord,CompensatorGrpcSyncClient compensatorGrpcSyncClient)  throws RpcException {
        BranchTransRequest branchTransRequest = RpcRequestBuilder.buildBranchTransRequest(scBranchRecord.getBusinessId(), Constants.COMMIT, scBranchRecord);
        BranchTransResponse branchTransResponse=transactionProcessor.commit(branchTransRequest,compensatorGrpcSyncClient);
        return branchTransResponse;
    }

    public BranchTransResponse processBranchTransRollback(ScBranchRecord scBranchRecord,CompensatorGrpcSyncClient compensatorGrpcSyncClient)  throws RpcException {
        BranchTransRequest branchTransRequest = RpcRequestBuilder.buildBranchTransRequest(scBranchRecord.getBusinessId(), Constants.CANCEL, scBranchRecord);
        BranchTransResponse branchTransResponse=transactionProcessor.rollback(branchTransRequest,compensatorGrpcSyncClient);
        return branchTransResponse;

    }



    @Override
    public void deleteBranch(Long tid) {
        transBranchInfoDao.delete(tid);
    }

    @Override
    public void deleteGlobal(Long tid) {
        transInfoDao.delete(tid);
    }

    @Override
    public boolean isBranchTransTryTimeout(ScBranchRecord scBranchRecord) {
        Date modifyTime = scBranchRecord.getModifyTime();
        Date now = new Date();
        return (now.getTime() - modifyTime.getTime()) > scBranchRecord.getTimeout();
    }

}
