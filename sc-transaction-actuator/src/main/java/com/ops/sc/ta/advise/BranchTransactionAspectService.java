package com.ops.sc.ta.advise;

import com.google.common.collect.Maps;
import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.utils.DistributeIdGenerator;
import com.ops.sc.common.context.TransContext;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.ta.buid.BranchTransBeanBuilder;
import com.ops.sc.ta.dao.LogDao;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import static com.ops.sc.common.constant.Constants.SC_LOGICAL_BRANCH_REGISTER_PARAM;

public class BranchTransactionAspectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchTransactionAspectService.class);

    @Resource(name = "scLogDao")
    private LogDao logDao;

    public void registerLogicalBranch(String appName, ProceedingJoinPoint joinPoint, String branchName, Long timeout,
                                      TimeoutType timeoutType) throws ScClientException{

        Long tid = TransactionContextRecorder.getTid();
        TransactionContextRecorder.setRoleContext(TransferRole.PARTICIPATOR);
        BranchTransRequest registerBranchRequest = BranchTransBeanBuilder
                .buildLogicalBranchParams(appName, joinPoint, branchName, timeout, timeoutType);
        String logicBranchId = registerLogicBranchAsync(registerBranchRequest);
        TransactionContextRecorder
                .setCurrentTransContext(new TransContext(String.valueOf(tid), true, Long.parseLong(logicBranchId), Long.parseLong(logicBranchId)));
       // registerBranchRequest = registerBranchRequest.toBuilder().setParentId(logicBranchId).build();

        Map<Object, Object> registerInfoMap = Maps.newHashMap();
        registerInfoMap.put(SC_LOGICAL_BRANCH_REGISTER_PARAM, registerBranchRequest);
        TransactionContextRecorder.getRegisterContext().set(registerInfoMap);
    }


    private String registerLogicBranchAsync(final BranchTransRequest request) {
        BranchTransRequest.Builder logicBranchBuilder = request.toBuilder().clone();
        String branchId = String.valueOf(DistributeIdGenerator.generateId());
        //logicBranchBuilder.setBranchId(branchId);
        /*TransActuatorRpcClientInit.getInstance().getRMClient(TransactionContextRecorder.getServerAddress())
                .registerBranchTransAsync(logicBranchBuilder.build());*/
        LOGGER.debug("Begin new logic branch transaction. tid: {}, branchId: {}", request.getTid(), branchId);
        return branchId;
    }

    public void updateTransLog(Long tid, TransStatus status, TransMode transMode, boolean isXA) throws ScClientException {
        try {

            int updateLogResult = logDao.updateXAInitiatorStatusByTid(tid, status.getValue());

            if (updateLogResult > 0) {
                LOGGER.debug("Update ScTransRecord status to {}. tid:{}", status, tid);
                //reportGlobalAsync(TransactionContextRecorder.getCurrentTransContext().getTransactionId().getTid(), status, transMode);
            } else {
                LOGGER.warn("Update ScTransRecord failed. tid:{} ", tid);
                throw new ScClientException(ClientErrorCode.LOCAL_DATABASE_FAILED, "Global transaction update failed!");
            }
        } catch (Exception e) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, "Global transaction update failed!", e.getCause());
        }
    }

    protected void notifyTcAsync(Long tid, TransStatus transStatus, TransMode transMode) {
    }



    protected void insertXATransLog(Long tid) throws SQLException {
        ScTransRecord scTransRecord = prepareInsertLog(tid);
        logDao.insertXATrans(scTransRecord);
    }

    private ScTransRecord prepareInsertLog(Long tid) {
        ScTransRecord scTransRecord = new ScTransRecord();
        scTransRecord.setTid(tid);
        //scTransRecord.setBranchId(tid);
        scTransRecord.setStatus(TransStatus.TRYING.getValue());
        //scTransRecord.setRollbackInfo(StringUtils.EMPTY);
        scTransRecord.setCreateTime(new Date());
        return scTransRecord;
    }
}
