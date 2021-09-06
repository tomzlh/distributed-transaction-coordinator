package com.ops.sc.ta.advise.impl;

import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.ta.advise.BranchRecoveryAspectService;
import com.ops.sc.ta.anno.BranchRecovery;
import com.ops.sc.ta.buid.BranchTransBeanBuilder;
import com.ops.sc.ta.advise.AbstractBranchAspectService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;


@Service("branchRecoveryAspectService")
public class BranchRecoveryAspectServiceImpl extends AbstractBranchAspectService implements BranchRecoveryAspectService {

    public Object recoveryBranchRegAdvise(ProceedingJoinPoint pjp, BranchRecovery branchRecovery)
            throws Throwable {
        return super.branchAdvise(pjp, branchRecovery);

    }

    @Override
    protected BranchTransRequest buildRequest(ProceedingJoinPoint pjp, Object branchAnnotation,
                                              String appName) throws ScClientException {

        BranchRecovery branchRecovery = (BranchRecovery) branchAnnotation;
        return BranchTransBeanBuilder.buildTccBranchRegisterParams(pjp, branchRecovery, appName);
    }

}
