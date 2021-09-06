package com.ops.sc.ta.advise;

import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.context.TransContext;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.BranchTransResponse;
import com.ops.sc.ta.service.AnnotationProcessService;
import com.ops.sc.ta.trans.DefaultTaCallOutService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;


public abstract class AbstractBranchAspectService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBranchAspectService.class);

    @Resource
    private AnnotationProcessService annotationProcessService;

    protected Object branchAdvise(ProceedingJoinPoint joinPoint, Object branchAnnotation) throws Throwable {
        if (annotationProcessService.isServiceDisable()) {
            return joinPoint.proceed();
        }
        TransactionContextRecorder.setRoleContext(TransferRole.PARTICIPATOR);
        TransactionContextRecorder.setCurrentTransMode(TransMode.TCC);
        try {
            Object result = processBranch(joinPoint, branchAnnotation);
            LOGGER.info("report status : {}, tid : {}, branchId : {}", TransStatus.TRY_SUCCEED,
                    TransactionContextRecorder.getTid(), TransactionContextRecorder.getBranchId());
            return result;
        } catch (Throwable e) {
            LOGGER.error("Start new branch transaction failed!", e);
            if (TransactionContextRecorder.isBranchIdExist()) {
                LOGGER.info("Report branch status : {}, tid : {}, branchId : {} ", TransStatus.TRY_FAILED,
                        TransactionContextRecorder.getTid(), TransactionContextRecorder.getBranchId());
            }
            throw e;
        } finally {
            TransactionContextRecorder.clearAllTransContext();
        }

    }

    private Object processBranch(ProceedingJoinPoint joinPoint, Object branchAnnotation) throws Throwable {
        TransactionContextRecorder.setCurrentTransContext(new TransContext(true));
        // 注册TCC模式分支事务，并初始化事务上下文
        BranchTransRequest request = buildRequest(joinPoint, branchAnnotation, annotationProcessService.getAppName());
        BranchTransResponse response = DefaultTaCallOutService.getInstance().registerBranch(request);
        if (!TransactionResponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, response.getBaseResponse().getMessage());
        }
        LOGGER.info("Start new branch transaction, tid : {}, branchId : {}", request.getTid(), response.getBranchId());
        TransactionContextRecorder.setCurrentTransContext(new TransContext(request.getBusinessId(),
                TransStatus.TRYING, Long.parseLong(response.getBranchId())));
        Object result = joinPoint.proceed();
        TransactionContextRecorder.updateTransStatusCurrentContext(TransStatus.TRY_SUCCEED);
        return result;
    }

    protected abstract BranchTransRequest buildRequest(ProceedingJoinPoint joinPoint, Object branchAnnotation,
                                                          String appName)  throws ScClientException ;
}
