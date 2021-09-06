package com.ops.sc.tc.advise;

import com.ops.sc.tc.anno.DistributeTrans;
import org.aspectj.lang.ProceedingJoinPoint;


public interface DistributeTransactionAspectService {
    Object transAdvise(ProceedingJoinPoint pjp, DistributeTrans distributeTrans) throws Throwable;
}
