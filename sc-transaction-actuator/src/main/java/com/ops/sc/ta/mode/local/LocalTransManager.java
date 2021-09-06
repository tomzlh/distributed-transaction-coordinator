package com.ops.sc.ta.mode.local;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.LocalInvokeType;
import com.ops.sc.common.enums.TransProcessMode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.ta.local.ResultProcessQueue;
import com.ops.sc.common.model.BranchInfo;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.ta.service.AnnotationProcessService;
import com.ops.sc.core.util.ApplicationUtils;
import com.ops.sc.ta.dao.DaoSupport;
import com.ops.sc.ta.trans.TccTaCallIn;
import com.ops.sc.rpc.dto.StateServiceRequest;
import com.ops.sc.rpc.dto.StateServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;


public class LocalTransManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalTransManager.class);

    private static AnnotationProcessService annotationProcessService = ApplicationUtils.getBean(AnnotationProcessService.class);

    private LocalTransManager() {
    }

    public static LocalTransManager getInstance() {
        return SingleInstanceHolder.instance;
    }

    /**
     * 调用commit方法并更新后续状态
     *
     * @param branchInfo
     * @return
     * @throws SQLException
     * @throws RpcException
     */
   public ResultProcessQueue commitAndReport(BranchInfo branchInfo) throws SQLException, RpcException,ScClientException {
        ResultProcessQueue result = ResultProcessQueue.done();
        long tid = branchInfo.getTid();
       Long branchId = branchInfo.getBid();
        long now = System.currentTimeMillis();
        LOGGER.debug("Ready to commit branch. tid: {} branchId: {}", tid, branchId);
        if (localInvoke(branchInfo, LocalInvokeType.COMMIT)) {
            stateReportSync(branchInfo, now, now, TransStatus.COMMIT_SUCCEED.getValue(), branchInfo.getRetryCount());
            DaoSupport.getTransBranchDao().updateStatusAndEndTimeById(branchInfo.getId(),
                    TransStatus.COMMIT_SUCCEED.getValue(), now, now);
            LOGGER.info("Commit branch success. tid: {} branchId: {}", tid, branchId);
        } else {
            int newRetryCount = branchInfo.getRetryCount() + 1;
            if (branchInfo.getRetryCount() >= Constants.MAX_RETRY_TIMES) {
                LOGGER.info("Commit fail and set status to confirm Failed. tid: {} branchId: {} retryCount: {}", tid,
                        branchId, newRetryCount);
                stateReportSync(branchInfo, now, now, TransStatus.COMMIT_FAILED.getValue(), newRetryCount);
                DaoSupport.getTransBranchDao().updateStatusAndEndTimeById(branchInfo.getId(),
                        TransStatus.COMMIT_FAILED.getValue(), now, now);
            } else {
                LOGGER.info("Commit fail and set retryCount to: {}.tid: {} branchId: {}", newRetryCount, tid, branchId);
                stateReportSync(branchInfo, now, branchInfo.getEndTime(), branchInfo.getStatus(), newRetryCount);
                result = ResultProcessQueue.toFailQueue();
            }
            DaoSupport.getTransBranchDao().updateRetryCount(branchInfo.getId(), newRetryCount);
        }
        return result;
    }

   public ResultProcessQueue rollbackAndReport(BranchInfo branch) throws SQLException, RpcException,ScClientException {
        Long tid = branch.getTid();
       Long branchId = branch.getBid();
        ResultProcessQueue result = ResultProcessQueue.done();
        long now = System.currentTimeMillis();
        LOGGER.debug("Ready to rollback branch. tid: {} branchId: {}", tid, branchId);
        if (localInvoke(branch, LocalInvokeType.ROLLBACK)) {
            stateReportSync(branch, now, now, TransStatus.CANCEL_SUCCEED.getValue(), branch.getRetryCount());
            DaoSupport.getTransBranchDao().updateStatusAndEndTimeById(branch.getId(), TransStatus.CANCEL_SUCCEED.getValue(),
                    now, now);
            LOGGER.info("Rollback branch success. tid: {} branchId: {}", tid, branchId);
        } else {
            int newRetryCount = branch.getRetryCount() + 1;
            if (branch.getRetryCount() >= Constants.MAX_RETRY_TIMES) {
                LOGGER.info("Rollback branch failed and set status to cancel failed. tid: {} branchId: {}", tid, branchId);
                stateReportSync(branch, now, now, TransStatus.CANCEL_FAILED.getValue(), branch.getRetryCount());
                DaoSupport.getTransBranchDao().updateStatusAndEndTimeById(branch.getId(),
                        TransStatus.CANCEL_FAILED.getValue(), now, now);
            } else {
                LOGGER.info("Rollback failed and set retryCount: {}, tid: {} branchId: {}", newRetryCount, tid, branchId);
                stateReportSync(branch, now, branch.getEndTime(), branch.getStatus(), newRetryCount);
                result = ResultProcessQueue.toFailQueue();
            }
            DaoSupport.getTransBranchDao().updateRetryCount(branch.getId(), newRetryCount);
        }
        return result;
    }

   public StateServiceResponse stateCheckRpc(Long tid, Long branchId) throws RpcException {
        StateServiceRequest request = StateServiceRequest.newBuilder().setTid(String.valueOf(tid)).setBranchId(String.valueOf(branchId)).build();
        /*return TransActuatorRpcClientInit.getInstance().getRMClient(annotationProcessService.getServerAddress()).stateCheckSync(request,
                RpcConstants.REQUEST_TIMEOUT_MILLS);*/
       return null;
    }

    /**
     * 同步上报
     *
     * @param branch
     * @throws RpcException
     */
   public void stateReportSync(BranchInfo branch, Long modifyTime, Long endTime, Integer status, Integer retryCount)
            throws RpcException {
        branch.setModifyTime(modifyTime);
        branch.setEndTime(endTime);
        branch.setStatus(status);
        branch.setRetryCount(retryCount);
       /*TransActuatorRpcClientInit.getInstance().getRMClient(annotationProcessService.getServerAddress()).statReportSync(request,
                RpcConstants.REQUEST_TIMEOUT_MILLS);*/
    }

    /**
     * 本地模式目前只支持TCC和事务消息
     *
     * @param branch
     * @param invokeType
     * @return
     * @throws SQLException
     */
    private boolean localInvoke(BranchInfo branch, LocalInvokeType invokeType) throws ScClientException,SQLException {
        Long tid = branch.getTid();
        Long branchId = branch.getBid();
        TransProcessMode mode = TransProcessMode.getModeByValue(branch.getTransType());
        if (TransProcessMode.TCC == mode) {
            return TccTaCallIn.getInstance().localInvoke(tid, branchId, branch.getResourceId(),
                    branch.getParams(), invokeType);
        }
        return false;
    }

    private static class SingleInstanceHolder {
        private static LocalTransManager instance = new LocalTransManager();
    }

}
