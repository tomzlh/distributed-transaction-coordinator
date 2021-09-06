package com.ops.sc.ta.aspect;

import com.ops.sc.ta.anno.BranchOperation;
import com.ops.sc.ta.advise.BranchOperationAspectService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Aspect
@Component("operateBranchAspect")
public class OperateBranchAspect implements Ordered {

    @Resource(name = "operateBranchAspectService")
    private BranchOperationAspectService branchOperationAspectService;

    @Pointcut("@annotation(com.ops.sc.ta.anno.BranchOperation)")
    public void operateBranchRegisterPoint() {

    }

    @Around("operateBranchRegisterPoint() && @annotation(branchOperation)")
    public Object transactionRegisterAdvise(ProceedingJoinPoint pjp, BranchOperation branchOperation)
            throws Throwable {
        return branchOperationAspectService.operateBranchRegisterAdvise(pjp, branchOperation);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
