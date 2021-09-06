package com.ops.sc.ta.local;


import com.google.common.collect.Lists;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.enums.TimeoutType;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.model.BranchInfo;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.ta.dao.DaoSupport;
import com.ops.sc.ta.mode.StateInfo;
import com.ops.sc.ta.mode.StateMonitor;
import com.ops.sc.ta.mode.local.LocalTransManager;
import com.ops.sc.rpc.dto.StateServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class LocalCompensateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalCompensateManager.class);

    private LocalCompensateManager() {
    }

    public static LocalCompensateManager getInstance() {
        return SingleInstanceHolder.instance;
    }

    /**
     * 对confirmFail或cancelFail的分支立即重试，修改为trySuccess，然后进行commit或者rollback
     *
     * @param tid
     * @param branchId
     */
    public void retryBranch(Long tid, Long branchId) {
        LOGGER.info("Start to retryBranch. tid: {} branchId: {}", tid, branchId);
        try {
            BranchInfo branchInfo = DaoSupport.getTransBranchDao().findByTidAndBranchId(tid, branchId);
            TransStatus branchStatus = TransStatus.getTransStatusByValue(branchInfo.getStatus());
            if (TransStatus.COMMIT_FAILED != branchStatus && TransStatus.CANCEL_FAILED != branchStatus) {
                LOGGER.warn("retryBranch status error. status: {}", branchStatus);
                return;
            }
            DaoSupport.getTransBranchDao().updateStatus(tid, branchId, branchStatus.getValue(),
                    TransStatus.TRY_SUCCEED.getValue());
            StateMonitor.getInstance().enQueue(new StateInfo(tid, branchId));
            LOGGER.info("Update to trySuccess and offer to queue success. tid: {} branchId: {}", tid, branchId);
        } catch (SQLException ex) {
            LOGGER.error("retryBranch error", ex);
        }
    }

    /**
     * 对timeout分支立即进行回滚，修改状态为tryFail，进行rollback即可
     *
     * @param tid
     * @param branchId
     */
    public void cancelTimeoutBranch(Long tid, Long branchId) {
        LOGGER.info("Start to cancelTimeoutBranch. tid: {} branchId: {}", tid, branchId);
        try {
            BranchInfo branchInfo = DaoSupport.getTransBranchDao().findByTidAndBranchId(tid, branchId);
            TransStatus branchStatus = TransStatus.getTransStatusByValue(branchInfo.getStatus());
            if (TransStatus.TRY_TIMEOUT != branchStatus) {
                LOGGER.warn("RetryFailBranch status error. status: {}", branchStatus);
                return;
            }
            DaoSupport.getTransBranchDao().updateStatus(tid, branchId, branchStatus.getValue(),
                    TransStatus.TRY_FAILED.getValue());
            StateMonitor.getInstance().enQueue(new StateInfo(tid, branchId));
            LOGGER.info("Update to tryFail and offer to queue success. tid: {} branchId: {}", tid, branchId);
        } catch (SQLException ex) {
            LOGGER.error("CancelTimeoutBranch error", ex);
        }
    }

    /**
     * RmCompensate使用
     *
     * 分支: try+timeout、其他状态则根据modifyTime校验是否outOfStayTime进行处理
     *
     */
    public void compensate() {
        LOGGER.debug("Compensate start...");
        List<Integer> statusList = Lists.newArrayList(TransStatus.TRYING.getValue(), TransStatus.TRY_SUCCEED.getValue(),
                TransStatus.TRY_FAILED.getValue(), TransStatus.COMMITTING.getValue(), TransStatus.CANCELLING.getValue());
        try {
            List<BranchInfo> transBranchList = DaoSupport.getTransBranchDao().findByStatusList(statusList);
            transBranchList.forEach(this::doCompensate);
        } catch (SQLException ex) {
            LOGGER.error("Compensate local db failed.", ex);
        }
        LOGGER.debug("Compensate end...");
    }

    private void doCompensate(BranchInfo branch) {
        Long tid = branch.getTid();
        Long branchId = branch.getBid();
        TransStatus branchStatus = TransStatus.getTransStatusByValue(branch.getStatus());
        LOGGER.debug("DoCompensate for tid: {} branchId: {} status: {}", tid, branchId, branchStatus);
        try {
            // tryFail或者trySuccess的modifyTime超时则重新进入队列处理
            if ((TransStatus.TRY_FAILED == branchStatus || TransStatus.TRY_SUCCEED == branchStatus)
                    && branch.exceedMaxTime()) {
                if (DaoSupport.getTransBranchDao().updateModifyTime(branch.getId(), System.currentTimeMillis()) > 0) {
                    LOGGER.info("EnQueue for outOfStatusStayTime branch. tid: {} branchId: {} status: {}", tid,
                            branchId, branchStatus);
                    StateMonitor.getInstance().enQueue(new StateInfo(tid, branchId));
                }
                return;
            }

            if (TransStatus.COMMITTING == branchStatus && branch.exceedMaxTime()) {
                if (DaoSupport.getTransBranchDao().updateModifyTime(branch.getId(), System.currentTimeMillis()) > 0) {
                    LOGGER.info("Commit outOfStatusStayTime branch. tid: {} branchId: {}", tid, branchId);
                    LocalTransManager.getInstance().commitAndReport(branch);
                }
                return;
            }

            if (TransStatus.CANCELLING == branchStatus && branch.exceedMaxTime()) {
                if (DaoSupport.getTransBranchDao().updateModifyTime(branch.getId(), System.currentTimeMillis()) > 0) {
                    LOGGER.info("Rollback outOfStatusStayTime branch. tid {} branchId: {}", tid, branchId);
                    LocalTransManager.getInstance().rollbackAndReport(branch);
                }
                return;
            }

            // trying超时
            if (TransStatus.TRYING == branchStatus && branch.isBranchTryTimeout()) {
                StateServiceResponse response = LocalTransManager.getInstance().stateCheckRpc(tid, branchId);
                if (!TransactionResponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
                    LOGGER.error("RmCompensate statCheck rpc return error.");
                    return;
                }
                TransStatus globalStatus = TransStatus.getTransStatusByValue(response.getStatus().getValue());
                LOGGER.info("DoCompensate for trying branch. tid: {} branchId: {} branchStatus: {} globalStatus:{}",
                        tid, branchId, branchStatus, globalStatus);
                // 全局成功则更新本地为成功
                if (TransStatus.COMMIT_SUCCEED == globalStatus || TransStatus.COMMIT_FAILED == globalStatus) {
                    DaoSupport.getTransBranchDao().updateStatus(tid, branchId, TransStatus.TRYING.getValue(),
                            TransStatus.TRY_SUCCEED.getValue());
                    StateMonitor.getInstance().enQueue(new StateInfo(tid, branchId));
                    return;
                }

                long now = System.currentTimeMillis();

                // 全局回滚，但是分支trying超时需要根据用户配置的分支超时策略处理
                if (TransStatus.CANCEL_SUCCEED == globalStatus || TransStatus.CANCEL_FAILED == globalStatus) {
                    if (TimeoutType.getByValue(branch.getTimeoutType()) == TimeoutType.ALARM) {
                        LOGGER.info(
                                "Update branch to tryTimeout. xid: {} branchId: {} branchStatus: {} globalStatus:{}",
                                tid, branchId, branchStatus, globalStatus);
                        LocalTransManager.getInstance().stateReportSync(branch, now, branch.getEndTime(),
                                TransStatus.TRY_TIMEOUT.getValue(), branch.getRetryCount());
                        DaoSupport.getTransBranchDao().updateStatus(tid, branchId, TransStatus.TRYING.getValue(),
                                TransStatus.TRY_TIMEOUT.getValue());
                    } else {
                        LOGGER.info("Update branch to tryFail. tid: {} branchId: {} branchStatus: {} globalStatus:{}",
                                tid, branchId, branchStatus, globalStatus);
                        DaoSupport.getTransBranchDao().updateStatus(tid, branchId, TransStatus.TRYING.getValue(),
                                TransStatus.TRY_FAILED.getValue());
                        StateMonitor.getInstance().enQueue(new StateInfo(tid, branchId));
                    }
                    return;
                }

                // 目前TC端逻辑全局超时不处理分支,客户端如果不更新状态,这样会导致每次RmCompensate都找到trying超时的事务
                if (TransStatus.TRY_TIMEOUT == globalStatus) {
                    // 只上报不处理
                    LOGGER.info("Report timeout trying branch to tc. tid: {} branchId: {} branchStatus: {}", tid,
                            branchId, branchStatus);
                    LocalTransManager.getInstance().stateReportSync(branch, branch.getModifyTime(),
                            branch.getEndTime(), branchStatus.getValue(), branch.getRetryCount());
                }

                // 如果全局是trying，分支无需处理，等全局checkBack进入新状态即可
            }
        } catch (SQLException ex) {
            LOGGER.error("RmCompensate local db fail. tid: {} branchId:{}", tid, branchId, ex);
        } catch (RpcException ex) {
            LOGGER.error("RmCompensate rpc fail. tid: {} branchId:{}", tid, branchId, ex);
        }catch (ScClientException se){
            LOGGER.error("RmCompensate local fail. tid: {} branchId:{}", tid, branchId, se);
        }
    }

    private static class SingleInstanceHolder {
        private static LocalCompensateManager instance = new LocalCompensateManager();
    }

}
