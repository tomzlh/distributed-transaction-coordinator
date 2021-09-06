package com.ops.sc.server.transaction;

import java.util.List;

import javax.annotation.Resource;

import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.trans.BaseTwoPhaseTransaction;
import com.ops.sc.core.glock.LockManager;
import com.ops.sc.server.service.impl.BranchTransStatusProcessorImpl;
import com.ops.sc.server.service.BranchTransService;
import com.ops.sc.server.service.GlobalTransService;
import com.ops.sc.common.model.TransLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ops.sc.common.enums.CallbackStrategy;
import com.ops.sc.common.enums.TransProcessMode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.trans.TransCommonResponse;

/**
 * 全局事务两阶段封装
 *
 */
@Service("globalTwoPhaseTransaction")
public class GlobalTwoPhaseTransImpl implements BaseTwoPhaseTransaction<ScTransRecord> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTwoPhaseTransImpl.class);

    @Resource
    private GlobalTransService globalTransService;

    @Resource
    private BranchTransService branchTransService;

    @Resource
    private BranchTransStatusProcessorImpl resourceCoordinator;

    @Resource
    private LockManager lockManager;

    /**
     * 事务准备
     *
     * @param transactionInfo
     * @return
     */
    @Override
    public TransCommonResponse saveTransInfo(ScTransRecord transactionInfo) {
        globalTransService.save(transactionInfo);
        return TransCommonResponse.builder().build().success();
    }

    @Override
    public TransCommonResponse saveTransInfo(List<ScTransRecord> baseInfos) {
        return null;
    }

    /**
     * 事务提交
     *
     * @param transactionInfo
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public TransCommonResponse commit(ScTransRecord transactionInfo) {
        TransStatus transStatus = TransStatus.getTransStatusByValue(transactionInfo.getStatus());
        Long tid = transactionInfo.getTid();
        List<ScBranchRecord> transBranchInfoList = branchTransService.getTransBranchInfoList(tid);
        boolean isTccMode = true;
        for (ScBranchRecord transBranchInfo : transBranchInfoList) { // FMT模式或者存在FMT模式的混合模式
            if (transBranchInfo.getTransMode().equals(TransProcessMode.FMT)
                    || transBranchInfo.getTransMode().equals(TransProcessMode.LOGIC_BRANCH)) {
                isTccMode = false;
                break;
            }
        }
        if (!isTccMode) {
            LOGGER.debug("Release lock for tid: {} before global commit", tid);
            lockManager.globalReleaseLock(tid);
        }
        if (!transStatus.equals(TransStatus.COMMITTING) && !transStatus.equals(TransStatus.COMMIT_FAILED)) {
            LOGGER.error("tid : {} status : {} can not to confirm ", transactionInfo.getTid(), transStatus);
            throw new IllegalStateException("GlobalTransConfirm status is not correct");
        }
        globalTransService.processBranchTransTryStatus(transactionInfo, transBranchInfoList);
        // 获取更新状态后的分支
        List<ScBranchRecord> newTransBranchInfoList = branchTransService.getTransBranchInfoList(tid);
        LOGGER.debug("tid : {} start to confirm", tid);
        CallbackStrategy callbackStrategy = CallbackStrategy.getCallbackStrategyByValue(transactionInfo.getCallbackStrategy());
        boolean isAllSuccess = true;
        boolean existBranchToCommit = false;
        for (ScBranchRecord transBranchInfo : newTransBranchInfoList) {
            TransCommonResponse transCommonResponse = null;
            TransStatus branchStatus = TransStatus.getTransStatusByValue(transBranchInfo.getStatus());
            LOGGER.debug("GlobalTrans confirm, branchId : {}, status : {}", transBranchInfo.getBid(),
                    transBranchInfo.getStatus());
            switch (branchStatus) {
            case COMMIT_FAILED:
                isAllSuccess = false;
                if (TransStatus.COMMITTING.getValue().equals(transactionInfo.getStatus())) {
                    globalTransService.updateStatusByTidAndStatus(transactionInfo.getTid(), transactionInfo.getStatus(),
                            TransStatus.COMMIT_FAILED.getValue());
                    transactionInfo.setStatus(TransStatus.COMMIT_FAILED.getValue());
                }
                break;
            case TRY_SUCCEED:
            case COMMITTING:
                transCommonResponse = resourceCoordinator.branchCommit(transBranchInfo);
                existBranchToCommit = !transCommonResponse.isNotExecute();
                isAllSuccess = transCommonResponse.isSuccess() && isAllSuccess;
                break;
            case COMMIT_SUCCEED:
                transCommonResponse = TransCommonResponse.builder().build().success();
                break;
            default:
                LOGGER.error("Status :{} not right for confirm ,branchId : {}", branchStatus,
                        transBranchInfo.getBid());
                throw new IllegalStateException("Branch status not right for confirm ");
            }

            if (callbackStrategy.equals(CallbackStrategy.IN_ORDER)) {
                if (transCommonResponse == null || !transCommonResponse.isSuccess()) {
                    break;
                }
            } else {
                // 不强制要求按序,其中一个分支不执行，此时为了简化流程，直接退出
                if (transCommonResponse != null && transCommonResponse.isNotExecute()) {
                    break;
                }
            }
        }
        if (isAllSuccess) {
            // 记录数据库全局事务状态
            LOGGER.info("tid : {} confirm all branchTrans success", transactionInfo.getTid());
            globalTransService.updateStatusAndEndTimeById(transactionInfo.getTid(), TransStatus.COMMIT_SUCCEED.getValue());
        } else if (existBranchToCommit) {
            // 避免补偿任务处理有confirmFail的分支重复打印无用日志
            LOGGER.info("tid : {} confirm all branchTrans, but not all branchTrans confirm success", transactionInfo.getTid());
        }
        return TransCommonResponse.builder().build().success();
    }

    /**
     * 事务回滚
     *
     * @param transactionInfo
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public TransCommonResponse rollback(ScTransRecord transactionInfo) {
        TransStatus transStatus = TransStatus.getTransStatusByValue(transactionInfo.getStatus());
        Long tid = transactionInfo.getTid();
        if (!transStatus.equals(TransStatus.CANCELLING) && !transStatus.equals(TransStatus.CANCEL_FAILED)) {
            LOGGER.error("Status : {} can not to confirm, tid : {}", transStatus, transactionInfo.getTid());
            throw new IllegalStateException("GlobalTransConfirm status not right");
        }
        globalTransService.processBranchTransTryStatus(transactionInfo, branchTransService.getTransBranchInfoList(tid));
        LOGGER.debug("Start to global cancel, tid : {}", transactionInfo.getTid());
        CallbackStrategy callbackStrategy = CallbackStrategy.getCallbackStrategyByValue(transactionInfo.getCallbackStrategy());
        boolean isAllSuccess = true;
        boolean isTccMode = true;
        boolean existBranchToRollback = false;
        List<ScBranchRecord> transBranchInfoList = branchTransService.getTransBranchInfoList(tid);
        for (ScBranchRecord scBranchRecord : transBranchInfoList) {
            if (scBranchRecord.getTransMode() == TransProcessMode.FMT.getValue()
                    || scBranchRecord.getTransMode() == TransProcessMode.LOGIC_BRANCH.getValue()) {
                isTccMode = false;
            }
            TransStatus branchStatus = TransStatus.getTransStatusByValue(scBranchRecord.getStatus());
            TransCommonResponse result = null;
            LOGGER.debug("GlobalTrans cancel, branchId: {}, status : {}", scBranchRecord.getBid(),
                    scBranchRecord.getStatus());
            switch (branchStatus) {
            case TRYING:
                isAllSuccess = false;
                break;
            case CANCEL_FAILED:
            case TRY_TIMEOUT:
                if (TransStatus.CANCELLING.getValue().equals(transactionInfo.getStatus())) {
                    globalTransService.updateStatusByTidAndStatus(transactionInfo.getTid(), transactionInfo.getStatus(),
                            TransStatus.CANCEL_FAILED.getValue());
                    transactionInfo.setStatus(TransStatus.CANCEL_FAILED.getValue());
                }
                isAllSuccess = false;
                break;
            case TRY_SUCCEED:
            case TRY_FAILED:
            case CANCELLING:
                // try-success/try-fail 正常流程执行cancel
                result = resourceCoordinator.branchRollBack(scBranchRecord);
                existBranchToRollback = !result.isNotExecute();
                isAllSuccess = result.isSuccess() && isAllSuccess;
                break;
            case CANCEL_SUCCEED:
                result = TransCommonResponse.builder().build().success();
                break;
            default:
                LOGGER.error("Status :{} not right for confirm ,branchId : {}", branchStatus,
                        scBranchRecord.getBid());
                throw new IllegalStateException("Branch status not right for confirm ");
            }
            if (callbackStrategy.equals(CallbackStrategy.IN_ORDER)) {
                if (result == null || !result.isSuccess()) {
                    break;
                }
            } else {
                if (result != null && result.isNotExecute()) {
                    break;
                }
            }
        }
        if (isAllSuccess) {
            LOGGER.info("Cancel all branchTrans success, tid : {} ", transactionInfo.getTid());
            globalTransService.updateStatusAndEndTimeById(transactionInfo.getTid(), TransStatus.CANCEL_SUCCEED.getValue());
            if (!isTccMode) {
                List<TransLock> transLockList = lockManager.queryTransLockList(tid);
                if (!transLockList.isEmpty()) {
                    LOGGER.info("Release abnormal lock after global rollback success,lockList: {}", transLockList);
                    lockManager.globalReleaseLock(tid);
                }
            }
        } else if (existBranchToRollback) {
            LOGGER.info("Cancel all branchTrans, but not all branchTrans cancel success, tid : {}", transactionInfo.getTid());
        }
        return TransCommonResponse.builder().build().success();
    }

}
