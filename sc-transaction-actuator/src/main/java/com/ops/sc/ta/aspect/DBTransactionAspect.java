package com.ops.sc.ta.aspect;

import com.ops.sc.ta.trans.support.ScTransactionAspectSupport;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Aspect
@Component("dbTransactionAspect")
public class DBTransactionAspect implements Ordered {

    @Resource(name = "scTransactionAspectSupport")
    private ScTransactionAspectSupport scTransactionAspectSupport;

    @Pointcut("@annotation(com.ops.sc.ta.anno.DataBaseTrans)")
    public void dbTransaction() {
    }

    @Around("dbTransaction()")
    public Object aroundTransaction(ProceedingJoinPoint pjp) throws Throwable {
        return scTransactionAspectSupport.runInTransaction(pjp);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
