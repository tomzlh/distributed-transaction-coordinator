package com.ops.sc.server.service.impl;

import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.trans.TransCommonResponse;
import com.ops.sc.common.trans.BaseTwoPhaseTransaction;
import com.ops.sc.core.glock.LockManager;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.BranchTransResponse;
import com.ops.sc.server.service.TransactionStatusService;
import com.ops.sc.server.service.BranchTransStatusProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.ops.sc.common.enums.TransProcessMode.FMT;


@Service
public class BranchTransStatusProcessorImpl implements BranchTransStatusProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchTransStatusProcessorImpl.class);

    @Autowired
    private LockManager lockManager;

    @Autowired
    private TransactionStatusService transactionStatusService;

    @Resource(name = "branchTwoPhaseTransaction")
    private BaseTwoPhaseTransaction branchTwoPhaseTransaction;

    @Override
    public BranchTransResponse registerBranch(final BranchTransRequest request) {
        return transactionStatusService.processBranchTrans(request);
    }

    @Override
    public BranchTransResponse executeBranch(BranchTransRequest request) {
        return transactionStatusService.processBranchTrans(request);
    }


    public TransCommonResponse branchCommit(ScBranchRecord branchInfo) {
        return branchTwoPhaseTransaction.commit(branchInfo);
    }

    public TransCommonResponse branchRollBack(ScBranchRecord branchInfo) {
        TransCommonResponse transCommonResponse = branchTwoPhaseTransaction.rollback(branchInfo);
        Long tid = branchInfo.getTid();
        Long branchId = branchInfo.getBid();
        if (!transCommonResponse.isSuccess()) {
            LOGGER.error("Rollback failed. tid : {}, branchId : {} .", tid, branchId);
        } else if (FMT.getValue()==branchInfo.getTransMode()) {
            LOGGER.debug("Rollback success and release lock for tid : {}, branchId : {} ", tid, branchId);
            lockManager.branchReleaseLock(tid, branchId);
        }
        return transCommonResponse;
    }

}
