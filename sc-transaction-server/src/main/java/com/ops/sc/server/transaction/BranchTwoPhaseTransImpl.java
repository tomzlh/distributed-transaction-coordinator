package com.ops.sc.server.transaction;

import javax.annotation.Resource;

import com.ops.sc.common.enums.TransProcessMode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.trans.BaseTwoPhaseTransaction;
import com.ops.sc.common.trans.TransCommonResponse;
import com.ops.sc.server.service.AlarmService;
import com.ops.sc.server.service.BranchTransService;
import com.ops.sc.server.service.GlobalTransService;
import com.ops.sc.common.enums.AlarmEvent;
import com.ops.sc.common.constant.ServerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 分支事务两阶段封装
 *
 */
@Service("branchTwoPhaseTransaction")
public class BranchTwoPhaseTransImpl implements BaseTwoPhaseTransaction<ScBranchRecord> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchTwoPhaseTransImpl.class);

    @Resource(name = "normalBranchTwoPhaseTransaction")
    private BaseTwoPhaseTransaction normalBranchTwoPhaseTransaction;

    @Resource(name = "mqBranchTwoPhaseTransaction")
    private BaseTwoPhaseTransaction mqBranchTwoPhaseTransaction;

    @Resource
    private BranchTransService branchTransService;

    @Resource
    private GlobalTransService globalTransService;

    @Resource
    private AlarmService alarmService;

    /**
     * 事务准备
     *
     * @param transBranchInfo
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public TransCommonResponse saveTransInfo(ScBranchRecord transBranchInfo) {
        TransProcessMode branchMode = TransProcessMode.getModeByValue(transBranchInfo.getTransMode());
        if (TransProcessMode.isRemoteMQBranch(branchMode)) {
            return mqBranchTwoPhaseTransaction.saveTransInfo(transBranchInfo);
        }
        return normalBranchTwoPhaseTransaction.saveTransInfo(transBranchInfo);
    }

    @Override
    public TransCommonResponse saveTransInfo(List<ScBranchRecord> baseInfos) {
        return null;
    }

    /**
     * 事务提交
     *
     * @param transBranchInfo
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public TransCommonResponse commit(ScBranchRecord transBranchInfo) {
        TransProcessMode branchMode = TransProcessMode.getModeByValue(transBranchInfo.getTransMode());
        TransCommonResponse transCommonResponse;
        if (TransProcessMode.isRemoteMQBranch(branchMode)) {
            transCommonResponse = mqBranchTwoPhaseTransaction.commit(transBranchInfo);
        } else {
            transCommonResponse = normalBranchTwoPhaseTransaction.commit(transBranchInfo);
        }
        if (transCommonResponse.isSuccess() || transCommonResponse.isNotExecute()) {
            return transCommonResponse;
        }
        AlarmEvent alarmEvent = AlarmEvent.TCC_BRANCH_CONFIRM_FAILED;
        if (TransProcessMode.isMQBranch(branchMode)) {
            alarmEvent = AlarmEvent.MSG_CONFIRM_FAILED;
        }
        if (transBranchInfo.getRetryCount() >= ServerConstants.MAX_RETRY_TIMES) {
            branchTransService.updateStatusById(transBranchInfo.getTid(), TransStatus.COMMIT_FAILED.getValue(),transBranchInfo.getRetryCount()+1,new Date());
            LOGGER.info("tid : {}, status change to confirm-fail, because branchId : {} confirm fail",
                    transBranchInfo.getTid(), transBranchInfo.getBid());
            globalTransService.updateStatusByTidAndStatus(transBranchInfo.getTid(), TransStatus.COMMITTING.getValue(),
                    TransStatus.COMMIT_FAILED.getValue());
            alarmService.sendAlarm(transBranchInfo.getTid(), transBranchInfo.getBid(), alarmEvent);
        }
        LOGGER.info("branchId : {} confirm fail,  retryCount : {} < maxRetryCount, retryCount + 1",
                transBranchInfo.getBid(), transBranchInfo.getRetryCount());
        branchTransService.updateRetryCount(transBranchInfo.getTid(), transBranchInfo.getRetryCount() + 1);
        return TransCommonResponse.builder().build().failed();
    }

    /**
     * 事务回滚
     *
     * @param transBranchInfo
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public TransCommonResponse rollback(ScBranchRecord transBranchInfo) {
        TransProcessMode branchMode = TransProcessMode.getModeByValue(transBranchInfo.getTransMode());
        TransCommonResponse transCommonResponse;
        if (TransProcessMode.isRemoteMQBranch(branchMode)) {
            transCommonResponse = mqBranchTwoPhaseTransaction.rollback(transBranchInfo);
        } else {
            transCommonResponse = normalBranchTwoPhaseTransaction.rollback(transBranchInfo);
        }
        if (transCommonResponse.isSuccess() || transCommonResponse.isNotExecute()) {
            return transCommonResponse;
        }
        AlarmEvent alarmEvent = AlarmEvent.TCC_BRANCH_CANCEL_FAILED;
        if (TransProcessMode.isMQBranch(branchMode)) {
            alarmEvent = AlarmEvent.MSG_CANCEL_FAILED;
        }
        if (transBranchInfo.getRetryCount() >= ServerConstants.MAX_RETRY_TIMES) {
            branchTransService.updateStatusById(transBranchInfo.getTid(), TransStatus.CANCEL_FAILED.getValue(),transBranchInfo.getRetryCount()+1,new Date());
            LOGGER.info("tid : {}, status change to cancel-fail, because branchId : {} cancel fail",
                    transBranchInfo.getTid(), transBranchInfo.getBid());
            globalTransService.updateStatusByTidAndStatus(transBranchInfo.getTid(), TransStatus.CANCELLING.getValue(),
                    TransStatus.CANCEL_FAILED.getValue());
            alarmService.sendAlarm(transBranchInfo.getTid(), transBranchInfo.getBid(), alarmEvent);
        }
        LOGGER.info("branchId : {} cancel fail,  retryCount : {}, add retryCount", transBranchInfo.getBid(),
                transBranchInfo.getRetryCount());
        branchTransService.updateRetryCount(transBranchInfo.getTid(), transBranchInfo.getRetryCount() + 1);
        return TransCommonResponse.builder().build().failed();
    }
}
