package com.ops.sc.server.transaction;

import javax.annotation.Resource;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.CallBackType;
import com.ops.sc.common.enums.TransProcessMode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.trans.TransCommonResponse;
import com.ops.sc.server.service.BranchTransService;
import com.ops.sc.common.enums.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service("normalBranchTwoPhaseTransaction")
public class NormalBranchTwoPhaseTransImpl extends BranchTwoPhaseTransImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(NormalBranchTwoPhaseTransImpl.class);

    @Resource
    private BranchTransService branchTransService;

    /**
     * 事务准备
     *
     * @param transBranchInfo
     * @return
     */
    @Override
    public TransCommonResponse saveTransInfo(ScBranchRecord transBranchInfo) {
        branchTransService.save(transBranchInfo);
        return  TransCommonResponse.builder().build().success();
    }

    /**
     * 事务提交
     *
     * @param transBranchInfo
     * @return
     */
    @Override
    public TransCommonResponse commit(ScBranchRecord transBranchInfo) {
        ExecutionResult result = ExecutionResult.SUCCEED;
        if (!(TransProcessMode.LOGIC_BRANCH==TransProcessMode.getModeByValue(transBranchInfo.getTransMode()))) {
            LOGGER.info("bid : {} start to confirm ", transBranchInfo.getBid());
            // 单次超时时间为连接超时时间+业务返回超时时间
            Long singleTimeout = Constants.DEFAULT_TIMEOUT + Constants.CONNECTION_TIMEOUT;
            if (!branchTransService.isNeedExecute(transBranchInfo, TransStatus.COMMITTING, singleTimeout)) {
                LOGGER.info("bid : {}, this status no need to confirm.", transBranchInfo.getBid());
                return TransCommonResponse.builder().build().notExecute();
            }
            result = branchTransService.executeBranchTrans(transBranchInfo, CallBackType.COMMIT);
            LOGGER.info("bid : {} confirm result : {}", transBranchInfo.getBid(), result.getDescription());
        }
        if (result == ExecutionResult.SUCCEED) {
            branchTransService.updateStatusById(transBranchInfo.getTid(), TransStatus.COMMIT_SUCCEED.getValue(),0,new Date());
            return TransCommonResponse.builder().build().success();
        }
        return TransCommonResponse.builder().build().failed();
    }

    /**
     * 事务回滚
     *
     * @param transBranchInfo
     * @return
     */
    @Override
    public TransCommonResponse rollback(ScBranchRecord transBranchInfo) {
        ExecutionResult result = ExecutionResult.SUCCEED;
        if (!(TransProcessMode.LOGIC_BRANCH==TransProcessMode.getModeByValue(transBranchInfo.getTransMode()))) {
            LOGGER.info("bid : {} start to cancel ", transBranchInfo.getBid());
            // 单次超时时间为连接超时时间+业务返回超时时间
            Long singleTimeout = Constants.DEFAULT_TIMEOUT + Constants.CONNECTION_TIMEOUT;
            if (!branchTransService.isNeedExecute(transBranchInfo, TransStatus.CANCELLING, singleTimeout)) {
                LOGGER.info("bid : {}, this status no need to cancel", transBranchInfo.getBid());
                return TransCommonResponse.builder().build().notExecute();
            }
            LOGGER.info("bid : {}, this status need to cancel.", transBranchInfo.getBid());
            result = branchTransService.executeBranchTrans(transBranchInfo, CallBackType.ROLLBACK);
            LOGGER.info("bid : {} cancel result : {}", transBranchInfo.getBid(), result.getDescription());
        }
        if (result == ExecutionResult.SUCCEED) {
            branchTransService.updateStatusById(transBranchInfo.getTid(), TransStatus.CANCEL_SUCCEED.getValue(),0,new Date());
            return TransCommonResponse.builder().build().success();
        }
        return TransCommonResponse.builder().build().failed();
    }
}
