package com.ops.sc.ta.mode;


import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.ta.local.ResultProcessQueue;
import com.ops.sc.common.model.BranchInfo;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.ta.dao.DaoSupport;
import com.ops.sc.ta.mode.local.LocalTransManager;
import com.ops.sc.rpc.dto.StateServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class StateMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateMonitor.class);

    private static final int MAX_ONCE_CHECK = 1000;
    private static final int MAX_COUNT_WAIT = MAX_ONCE_CHECK * 50;

    private static final long INITIAL_DELAY = 1;

    private static final ScheduledExecutorService CHECK_SCHEDULE = Executors.newSingleThreadScheduledExecutor();
    private static final ScheduledExecutorService RETRY_SCHEDULE = Executors.newSingleThreadScheduledExecutor();
    private static final int CHECK_CRON = 1;
    private static final int RETRY_CHECK_CRON = 10;
    private static LinkedBlockingQueue<StateInfo> statCheckQueue = new LinkedBlockingQueue<>(MAX_COUNT_WAIT);
    private static LinkedBlockingQueue<StateInfo> failedQueue = new LinkedBlockingQueue<>();

    private StateMonitor() {
    }

    /**
     * 返回单例对象
     *
     * @return
     */
    public static StateMonitor getInstance() {
        return InstanceHolder.instance;
    }

    public static void shutdown() {
        CHECK_SCHEDULE.shutdown();
        RETRY_SCHEDULE.shutdown();
    }

    public void enQueue(StateInfo info) {
        // 队列已满返回false
        LOGGER.debug("Add statCheck. tid: {} branchId: {}", info.getTid(), info.getBranchId());
        boolean in = statCheckQueue.offer(info);
        if (!in) {
            LOGGER.warn("Add statCheck failed. tid: {} branchId: {}", info.getTid(), info.getBranchId());
        }
    }

    /**
     * 同库模式二阶段StatCheckJob
     */
    public void start() {
        CHECK_SCHEDULE.scheduleWithFixedDelay(this::check, INITIAL_DELAY, CHECK_CRON, TimeUnit.SECONDS);
        RETRY_SCHEDULE.scheduleWithFixedDelay(this::retryCheck, INITIAL_DELAY, RETRY_CHECK_CRON, TimeUnit.SECONDS);
    }

    private void check() {
        int checkCount = 0;
        List<StateInfo> waitNextCheckList = new ArrayList<>();
        while (!statCheckQueue.isEmpty() && checkCount <= MAX_ONCE_CHECK) {
            StateInfo info = statCheckQueue.poll();
            ResultProcessQueue result = handleStatCheck(info);
            LOGGER.debug("Handle statCheck result: {},tid: {} branchId:{}, queue size: {}, checkCount:{}", result,
                    info.getTid(), info.getBranchId(), statCheckQueue.size(), checkCount);
            if (result.isToFailQueue()) {
                LOGGER.info("Local branch phase2 failed. tid {} branchId: {}", info.getTid(), info.getBranchId());
                failedQueue.offer(info);
            }
            if (result.isWaitNextCheck()) {
                waitNextCheckList.add(info);
            }
            checkCount++;
        }
        if (!waitNextCheckList.isEmpty()) {
            waitNextCheckList.forEach((stateInfo -> statCheckQueue.offer(stateInfo)));
        }
    }

    private void retryCheck() {
        int retryCount = 0;
        List<StateInfo> failList = new ArrayList<>();
        while (!failedQueue.isEmpty() && retryCount <= MAX_ONCE_CHECK) {
            StateInfo info = failedQueue.poll();
            ResultProcessQueue result = handleStatCheck(info);
            if (!result.isDone()) {
                LOGGER.info("Retry failed and put into failedQueue again. tid {} branchId: {}", info.getTid(),
                        info.getBranchId());
                failList.add(info);
            }
            retryCount++;
        }
        if (!failList.isEmpty()) {
            failList.forEach((stateInfo) -> failedQueue.offer(stateInfo));
        }
    }

    /**
     * 客户端StatCheckJob使用
     *
     * 分支：try_fail或try_success 如果是retry还有confirming或canceling
     *
     * 全局：trying、confirm_success、confirm_fail、cancel_success、cancel_fail或try_timeout
     *
     * @param stateInfo
     * @return
     */
    private ResultProcessQueue handleStatCheck(StateInfo stateInfo) {
        Long tid = stateInfo.getTid();
        Long branchId = stateInfo.getBranchId();
        LOGGER.debug("Handle statCheck for tid: {} branchId: {}", tid, branchId);
        try {

            BranchInfo branchInfo = DaoSupport.getTransBranchDao().findByTidAndBranchId(tid, branchId);
            if (branchInfo == null) {
                LOGGER.info("Branch is not exist. tid: {} branchId: {} ", tid, branchId);
                return ResultProcessQueue.done();
            }

            TransStatus branchStatus = TransStatus.getTransStatusByValue(branchInfo.getStatus());
            if (branchStatus.isFinalStatus()) {
                LOGGER.debug("Branch is in final status.  tid: {} branchId: {} status: {}", tid, branchId,
                        branchStatus);
                return ResultProcessQueue.done();
            }

            // 分支已经是CONFIRMING，防止和RmCompensate冲突修改ModifyTime
            if (TransStatus.COMMITTING == branchStatus && branchInfo.exceedMaxTime()) {
                if (DaoSupport.getTransBranchDao().updateModifyTime(branchInfo.getId(), System.currentTimeMillis()) > 0) {
                    LOGGER.info("Commit outOfStatusStayTime confirming branch. tid: {} branchId: {}", tid, branchId);
                    return LocalTransManager.getInstance().commitAndReport(branchInfo);
                }
            }

            // 分支已经是CANCELLING，防止和RmCompensate冲突修改ModifyTime
            if (TransStatus.CANCELLING == branchStatus && branchInfo.exceedMaxTime()) {
                if (DaoSupport.getTransBranchDao().updateModifyTime(branchInfo.getId(), System.currentTimeMillis()) > 0) {
                    LOGGER.info("Rollback outOfStatusStayTime cancelling branch. tid: {} branchId: {}", tid, branchId);
                    return LocalTransManager.getInstance().rollbackAndReport(branchInfo);
                }
            }

            StateServiceResponse response = LocalTransManager.getInstance().stateCheckRpc(tid, branchId);
            if (!TransactionResponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
                if (response.getBaseResponse() != null) {
                    LOGGER.error("StatCheck error code: {}, tid: {}", response.getBaseResponse().getCode(), tid);
                }
                return ResultProcessQueue.toFailQueue();
            }
            TransStatus globalStatus = TransStatus.getTransStatusByValue(response.getStatus().getValue());

            LOGGER.debug("StateCheck global status: {} tid: {}", globalStatus, tid);

            if (TransStatus.TRYING == globalStatus) {
                return ResultProcessQueue.waitNextCheck();
            }

            if (TransStatus.TRY_TIMEOUT == globalStatus) {
                LOGGER.info(
                        "Global transaction is tryTimeout and branch wait for dev ops. tid:{} branchId: {} status: {}",
                        tid, branchId, branchStatus);
                return ResultProcessQueue.done();
            }

            // 全局提交
            if (TransStatus.COMMIT_SUCCEED == globalStatus || TransStatus.COMMIT_FAILED == globalStatus) {
                if (DaoSupport.getTransBranchDao().updateStatus(tid, branchId, branchInfo.getStatus(),
                        TransStatus.COMMITTING.getValue()) > 0) {
                    return LocalTransManager.getInstance().commitAndReport(branchInfo);
                }
            }

            // 全局回滚
            if (TransStatus.CANCEL_SUCCEED == globalStatus || TransStatus.CANCEL_FAILED == globalStatus) {
                if (DaoSupport.getTransBranchDao().updateStatus(tid, branchId, branchInfo.getStatus(),
                        TransStatus.CANCELLING.getValue()) > 0) {
                    return LocalTransManager.getInstance().rollbackAndReport(branchInfo);
                }
            }

        } catch (SQLException ex) {
            LOGGER.error("StateCheck db fail. tid: {} branchId:{}", tid, branchId, ex);
            return ResultProcessQueue.toFailQueue();
        } catch (RpcException ex) {
            LOGGER.error("State check rpc failed. tid: {} branchId:{}", tid, branchId, ex);
            return ResultProcessQueue.toFailQueue();
        } catch (Exception ex) {
            LOGGER.error("State check error. tid: {} branchId:{}", tid, branchId, ex);
            return ResultProcessQueue.toFailQueue();
        }
        return ResultProcessQueue.done();
    }

    private static class InstanceHolder {
        static StateMonitor instance = new StateMonitor();
    }
}
