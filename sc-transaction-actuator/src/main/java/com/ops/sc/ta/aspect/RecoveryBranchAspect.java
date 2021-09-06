package com.ops.sc.ta.aspect;

import com.ops.sc.ta.anno.BranchRecovery;
import com.ops.sc.ta.advise.BranchRecoveryAspectService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Aspect
@Component("recoveryBranchAspect")
public class RecoveryBranchAspect implements Ordered {

    @Resource(name = "branchRecoveryAspectService")
    private BranchRecoveryAspectService branchRecoveryAspectService;

    @Pointcut("@annotation(com.ops.sc.ta.anno.BranchRecovery)")
    public void recoveryBranchRegisterPoint() {

    }

    @Around("recoveryBranchRegisterPoint() && @annotation(branchRecovery)")
    public Object recoveryBranchRegisterAdvise(ProceedingJoinPoint pjp, BranchRecovery branchRecovery)
            throws Throwable {
        return branchRecoveryAspectService.recoveryBranchRegAdvise(pjp, branchRecovery);
    }

    /**
     * 保证比Spring的事务注解优先执行
     *
     * @return
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

}
