package com.ops.sc.ta.advise;

import com.ops.sc.ta.anno.BranchRecovery;
import org.aspectj.lang.ProceedingJoinPoint;

public interface BranchRecoveryAspectService {
    Object recoveryBranchRegAdvise(ProceedingJoinPoint pjp, BranchRecovery branchRecovery)
            throws Throwable;
}
