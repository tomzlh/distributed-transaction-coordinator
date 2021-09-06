package com.ops.sc.ta.advise;

import com.ops.sc.ta.anno.BranchOperation;
import org.aspectj.lang.ProceedingJoinPoint;


public interface BranchOperationAspectService {
    Object operateBranchRegisterAdvise(ProceedingJoinPoint pjp, BranchOperation branchOperation) throws Throwable;
}
