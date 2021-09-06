package com.ops.sc.ta.local;

import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.common.utils.DistributeIdGenerator;
import com.ops.sc.common.model.BranchInfo;
import com.ops.sc.ta.dao.DaoSupport;
import com.ops.sc.ta.mode.StateInfo;
import com.ops.sc.ta.mode.StateMonitor;
import com.ops.sc.ta.trans.TaCallOut;
import com.ops.sc.rpc.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LocalTransCallCallOut implements TaCallOut {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalTransCallCallOut.class);

    private static final int THREAD_POOL_SIZE = 2;
    private static ExecutorService executors = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private LocalTransCallCallOut() {
    }

    public static LocalTransCallCallOut getInstance() {
        return InstanceHolder.instance;
    }

    public static void shutdown() {
        executors.shutdown();
    }

    /**
     * 分支注册
     *
     * @param request
     * @return
     */
    @Override
    public BranchTransResponse registerBranch(BranchTransRequest request)  throws ScClientException {
        Long branchId = DistributeIdGenerator.generateId();
        BranchInfo branchInfo = new BranchInfo();
        //branchInfo.setParams(CommonUtils.toByteArray(request.getParams()));
        branchInfo.setRetryCount(0);
        //branchInfo.setParentId(request.getParentId());
        branchInfo.setTransType(request.getBranchType().getValue());
        //branchInfo.setStatus(request.getStatus().getValue());
        //branchInfo.setTimeout(request.getTimeout().getValue());
        //branchInfo.setTimeoutType(request.getTimeoutStrategy().getValue());
        //branchInfo.setTransactionName(request.getTransactionName());
        branchInfo.setTid(Long.parseLong(request.getTid()));
        branchInfo.setBid(branchId);
        long now = System.currentTimeMillis();
        branchInfo.setCreateTime(now);
        branchInfo.setModifyTime(now);
        try {
            DaoSupport.getTransBranchDao().insert(branchInfo);
        } catch (SQLException ex) {
            throw new ScClientException(ClientErrorCode.LOCAL_DATABASE_FAILED, ex.getMessage(), ex.getCause());
        }
        LOGGER.debug("Save new local branch. tid: {} branchId: {}", request.getTid(), branchId);
        BranchTransResponse.Builder responseBuilder = BranchTransResponse.newBuilder();
        responseBuilder.setBranchId(String.valueOf(branchId));
        responseBuilder.setBaseResponse(ParentResponse.newBuilder().setCode(TransactionResponseCode.SUCCESS.getCode()).build());
        return responseBuilder.build();
    }

    @Override
    public BranchTransInfoList queryBranchInfoList(BranchTransQueryRequest branchTransQueryRequest) {
        return null;
    }

    private static class InstanceHolder {
        static LocalTransCallCallOut instance = new LocalTransCallCallOut();
    }

    private class LocalUpdateTask implements Runnable {
        Long tid;
        Long branchId;
        Integer status;

        LocalUpdateTask(Long tid, Long branchId, Integer status) {
            this.tid = tid;
            this.branchId = branchId;
            this.status = status;
        }

        @Override
        public void run() {
            try {
                LOGGER.debug("Async update branch status. tid: {} branchId: {} status: {}", tid, branchId, status);
                if (DaoSupport.getTransBranchDao().updateStatus(tid, branchId, TransStatus.TRYING.getValue(), status) > 0) {
                    StateMonitor.getInstance().enQueue(new StateInfo(tid, branchId));
                    return;
                }
                LOGGER.warn("Branch status not in trying when async update. tid: {} branchId: {}", tid, branchId);
            } catch (SQLException ex) {
                LOGGER.error("Update local branch status fail.", ex);
                throw new ScTransactionException(TransactionResponseCode.LOCK_CONFLICT, ex.getMessage());
            }
        }
    }

}
