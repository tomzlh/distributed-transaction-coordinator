package com.ops.sc.server.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.ops.sc.common.enums.*;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.bean.TransInfoQueryParams;
import com.ops.sc.server.dao.TransBranchInfoDao;
import com.ops.sc.server.dao.TransInfoDao;
import com.ops.sc.server.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ops.sc.common.constant.ServerConstants;
import org.springframework.transaction.annotation.Transactional;


@Service
public class GlobalTransServiceImpl implements GlobalTransService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTransServiceImpl.class);

    @Autowired
    private TransInfoDao transInfoDao;

    @Autowired
    private TransBranchInfoDao transBranchInfoDao;

    @Autowired
    private BranchTransService branchTransService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    @Lazy
    private TransExecuteService transExecuteService;

    @Autowired
    private CallAction callAction;

    @Override
    public void save(ScTransRecord scTransRecord) {
        transInfoDao.save(scTransRecord);
    }

    @Transactional
    @Override
    public void saveTransAndBranchTrans(ScTransRecord scTransRecord) {
        transInfoDao.save(scTransRecord);
        branchTransService.save(scTransRecord.getBranchTransactionList());
    }

    @Override
    public List<ScTransRecord> findByStatus(List<Integer> statusList) {
        return transInfoDao.findByStatus(statusList);
    }

    @Override
    public List<ScTransRecord> find(TransInfoQueryParams queryParams) {
        return transInfoDao.findByConditions(queryParams);
    }

    @Override
    public void updateStatusAndEndTimeById(Long id, Integer status) {
        transInfoDao.updateStatusAndEndTimeById(id, status);
    }

    @Override
    public ScTransRecord getByTid(Long tid) {
        return transInfoDao.findByTid(tid);
    }

    @Override
    public ScTransRecord getByBusinessId(String businessId) {
        return transInfoDao.findByBusinessId(businessId);
    }

    @Override
    public void delete(Long tid) {
        transInfoDao.delete(tid);
    }

    /**
     * 通过fromStatus修改全局事务状态
     *
     * @param tid
     * @param fromStatus
     * @param toStatus
     * @return
     */
    @Override
    public int updateStatusByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus) {
        return transInfoDao.updateStatusByTidAndStatus(tid, fromStatus, toStatus);
    }

    @Override
    public int updateStatusByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus, Integer retryCount) {
        return transInfoDao.updateStatusRetryCountByTidAndStatus(tid, fromStatus, toStatus,retryCount);
    }

    @Override
    public int updateRetryCount(Long id, Integer retryCount) {
        return transInfoDao.updateRetryCount(id,retryCount);
    }

    @Override
    public int updateStatusByTids(List<Long> tids, Integer toStatus) {
        return transInfoDao.updateStatusByTids(tids,toStatus);
    }

    @Override
    public Boolean isGlobalTransTryTimeout(ScTransRecord transactionInfo) {
        if (TransStatus.getTransStatusByValue(transactionInfo.getStatus()) != TransStatus.TRYING) {
            throw new IllegalStateException();
        }
        Date createTime = transactionInfo.getCreateTime();
        Date now = new Date();
        return (now.getTime() - createTime.getTime()) > transactionInfo.getTimeout();
    }

    @Override
    public TransStatus globalTransStatusCheckBack(ScTransRecord transactionInfo) {
        Long tid = transactionInfo.getTid();
        LOGGER.info("Start to checkBack. tid: {}", tid);
        GlobalTransStatus checkBackResult = callAction.globalCheckBackExecute(transactionInfo);
        TransStatus globalTryStatus = checkBackResultToTransStatus(checkBackResult);
        ServerMode mode = ServerMode.REMOTE;

        LOGGER.info("CheckBack result: status: {}, tid: {}", globalTryStatus, tid);
        if (globalTryStatus == TransStatus.NOT_EXIST && transactionInfo.getRetryCount() == 0) {
            globalTryStatus = TransStatus.TRYING;
        }
        if (globalTryStatus == TransStatus.TRYING) {
            if (transactionInfo.getRetryCount() >= ServerConstants.MAX_RETRY_TIMES) {
                TimeoutType strategy = TimeoutType.getByValue(transactionInfo.getTimeoutType());
                alarmService.sendAlarm(transactionInfo.getTid(), null, AlarmEvent.TCC_GLOBAL_TRY_TIMEOUT);
                if (strategy == TimeoutType.ALARM) {
                    LOGGER.info("Set status to try_timeout. tid: {}", transactionInfo.getTid());
                    transInfoDao.updateStatusByTidAndStatus(transactionInfo.getTid(), TransStatus.TRYING.getValue(),
                            TransStatus.TRY_TIMEOUT.getValue());
                    return TransStatus.TRY_TIMEOUT;
                } else {
                    LOGGER.info("Set status to try_fail. tid: {}", transactionInfo.getTid());
                    globalTryStatus = TransStatus.TRY_FAILED;
                }
            } else {
                // 没有达到最大次数，retryCount+1之后，直接返回不做操作
                int newRetryCount = transactionInfo.getRetryCount() + 1;
                LOGGER.info("CheckBack result is trying and set retryCount: {}. tid: {}", newRetryCount,
                        transactionInfo.getTid());
                transInfoDao.updateRetryCount(transactionInfo.getTid(), newRetryCount);
                return TransStatus.TRYING;
            }
        }

        TransStatus targetStatus;
        if (ServerMode.REMOTE == mode) {
            targetStatus = globalTryStatus == TransStatus.TRY_FAILED || globalTryStatus == TransStatus.NOT_EXIST
                    ? TransStatus.CANCELLING : TransStatus.COMMITTING;
        } else {
            targetStatus = globalTryStatus == TransStatus.TRY_FAILED || globalTryStatus == TransStatus.NOT_EXIST
                    ? TransStatus.CANCEL_SUCCEED : TransStatus.COMMIT_SUCCEED;
        }
        if (transInfoDao.updateStatusByTidAndStatus(transactionInfo.getTid(), TransStatus.TRYING.getValue(),
                targetStatus.getValue()) == 1) {
            transactionInfo.setStatus(targetStatus.getValue());
            return targetStatus;
        }
        return null;
    }


    @Override
    public void checkBackTimeoutGlobalTrans(ScTransRecord transactionInfo) {
        if (!TransStatus.TRY_TIMEOUT.getValue().equals(transactionInfo.getStatus())) {
            throw new IllegalStateException("UnSupport status when checkBack timeout global transaction");
        }

        LOGGER.info("Start to checkBack timeoutMills global transaction, tid: {}", transactionInfo.getTid());
        GlobalTransStatus checkBackResult = callAction.globalCheckBackExecute(transactionInfo);

        TransStatus globalTryStatus = checkBackResultToTransStatus(checkBackResult);

        LOGGER.info("CheckBackTimeoutGlobalTransaction globalTransactionTryStatus: {} tid: {} ", globalTryStatus, transactionInfo.getTid());

        if (globalTryStatus == TransStatus.TRYING) {
            transInfoDao.updateRetryCount(transactionInfo.getTid(), transactionInfo.getRetryCount() + 1);
        } else {
            TransStatus targetStatus = globalTryStatus == TransStatus.TRY_FAILED || globalTryStatus == TransStatus.NOT_EXIST
                    ? TransStatus.CANCELLING : TransStatus.COMMITTING;
            if (transInfoDao.updateStatusByTidAndStatus(transactionInfo.getTid(), transactionInfo.getStatus(),
                    targetStatus.getValue()) != 0) {
                /*TransEventType eventType = globalTryStatus.equals(TransStatus.TRY_SUCCEED) ? TransEventType.TCC_CONFIRM
                        : TransEventType.TCC_CANCEL;
                transExecuteService.submitGlobalTrans(new TransReportEvent(transactionInfo.getTid(), eventType));*/
            }
        }
    }


    public void processBranchTransTryStatus(ScTransRecord transactionInfo, List<ScBranchRecord> transBranchInfoList) {
        TransStatus globalTransStatus = TransStatus.getTransStatusByValue(transactionInfo.getStatus());
        if (globalTransStatus != TransStatus.COMMITTING && globalTransStatus != TransStatus.CANCELLING
                && globalTransStatus != TransStatus.COMMIT_FAILED && globalTransStatus != TransStatus.CANCEL_FAILED) {
            LOGGER.error("Not in (CONFIRMING, CANCELLING, CONFIRM-FAIL, CANCEL-FAIL),tid: {}, status: {} ",
                    transactionInfo.getTid(), globalTransStatus);
            throw new IllegalStateException("globalTransStatus not right");
        }

        if (globalTransStatus == TransStatus.COMMITTING || globalTransStatus == TransStatus.COMMIT_FAILED) {
            // 分支事务处于try阶段，状态置为try-success
            for(ScBranchRecord transBranchInfo:transBranchInfoList){
                TransStatus branchTransStatus = TransStatus.getTransStatusByValue(transBranchInfo.getStatus());
                if (branchTransStatus == TransStatus.TRYING || branchTransStatus == TransStatus.TRY_FAILED
                        || branchTransStatus == TransStatus.TRY_TIMEOUT) {
                    transBranchInfoDao.updateStatusByBranchIdAndStatus(transBranchInfo.getBid(),
                            Collections.singletonList(transBranchInfo.getStatus()), TransStatus.TRY_SUCCEED.getValue());
                }
            }
            return;
        }
        // CANCELLING状态的全局事务
        processGlobalCancel(transactionInfo, transBranchInfoList);

    }

    private void processGlobalCancel(ScTransRecord scTransRecord, List<ScBranchRecord> transBranchInfoList) {
        for(ScBranchRecord transBranchInfo: transBranchInfoList){
            if (TransStatus.TRYING.getValue().equals(transBranchInfo.getStatus())
                    && branchTransService.isBranchTransTryTimeout(transBranchInfo)) {
                // 超时情况 依据策略进行
                TimeoutType strategy = TimeoutType
                        .getByValue(transBranchInfo.getTimeoutType());
                TransStatus targetStatus = strategy == TimeoutType.ALARM ? TransStatus.TRY_TIMEOUT : TransStatus.TRY_FAILED;
                // 需要依据原状态修改，防止此时分支事务上报状态
                transBranchInfoDao.updateStatusByBranchIdAndStatus(transBranchInfo.getBid(),
                        Collections.singletonList(transBranchInfo.getStatus()), targetStatus.getValue());
                alarmService.sendAlarm(transBranchInfo.getTid(), transBranchInfo.getBid(),
                        AlarmEvent.TCC_BRANCH_TRY_TIMEOUT);
                if (targetStatus == TransStatus.TRY_TIMEOUT && TransStatus.CANCELLING.getValue().equals(scTransRecord.getStatus())) {
                    transInfoDao.updateStatusByTidAndStatus(transBranchInfo.getTid(), TransStatus.CANCELLING.getValue(),
                            TransStatus.CANCEL_FAILED.getValue());
                    scTransRecord.setStatus(TransStatus.CANCEL_FAILED.getValue());
                }
            }
        }
    }

    /**
     * checkBackResult 转换为transStatus
     *
     * @param checkBackResult
     * @return
     */
    private TransStatus checkBackResultToTransStatus(GlobalTransStatus checkBackResult) {
        TransStatus globalTransTryStatus = TransStatus.TRYING;
        if (GlobalTransStatus.SUCCESS.equals(checkBackResult)) {
            globalTransTryStatus = TransStatus.TRY_SUCCEED;
        } else if (GlobalTransStatus.FAILED.equals(checkBackResult)) {
            globalTransTryStatus = TransStatus.TRY_FAILED;
        } else if (GlobalTransStatus.NOT_EXIST.equals(checkBackResult)) {
            globalTransTryStatus = TransStatus.NOT_EXIST;
        }
        return globalTransTryStatus;
    }

}
