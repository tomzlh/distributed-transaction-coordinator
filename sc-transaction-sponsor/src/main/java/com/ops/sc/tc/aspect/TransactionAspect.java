package com.ops.sc.tc.aspect;


import com.ops.sc.tc.advise.DistributeTransactionAspectService;
import com.ops.sc.tc.anno.DistributeTrans;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


//@Aspect
//@Component("transactionAspect")
public class TransactionAspect implements Ordered {

    protected static final Integer SC_ASPECT_ORDER = 0;

    @Resource(name = "transactionAspectService")
    private DistributeTransactionAspectService distributeTransactionAspectService;

    @Pointcut("@annotation(com.ops.sc.tc.anno.DistributeTrans)")
    public void transactionRegisterPoint() {

    }

    @Around("transactionRegisterPoint() && @annotation(distributeTrans)")
    public Object transactionRegisterAdvise(ProceedingJoinPoint pjp, DistributeTrans distributeTrans)
            throws Throwable {
        return distributeTransactionAspectService.transAdvise(pjp, distributeTrans);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
