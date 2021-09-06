package com.ops.sc.ta.advise.impl;

import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.ta.advise.AbstractBranchAspectService;
import com.ops.sc.ta.advise.BranchOperationAspectService;
import com.ops.sc.ta.anno.BranchOperation;
import com.ops.sc.ta.buid.BranchTransBeanBuilder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;

@Service("operateBranchAspectService")
public class OperateBranchAspectServiceImpl extends AbstractBranchAspectService implements BranchOperationAspectService {

    @Override
    public Object operateBranchRegisterAdvise(ProceedingJoinPoint pjp, BranchOperation branchOperation)
            throws Throwable {

        return super.branchAdvise(pjp, branchOperation);

    }

    @Override
    protected BranchTransRequest buildRequest(ProceedingJoinPoint pjp, Object branchAnnotation,
                                              String appName)  throws ScClientException {
        BranchOperation branchOperation = (BranchOperation) branchAnnotation;
        return BranchTransBeanBuilder.buildTccBranchRegisterParams(pjp, branchOperation, appName);
    }
}
