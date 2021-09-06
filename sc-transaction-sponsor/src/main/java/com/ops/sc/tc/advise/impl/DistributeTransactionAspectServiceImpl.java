package com.ops.sc.tc.advise.impl;

import com.google.protobuf.UInt32Value;
import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.utils.AnnotationParamUtils;
import com.ops.sc.tc.advise.DistributeTransactionAspectService;
import com.ops.sc.tc.anno.DistributeTrans;
import com.ops.sc.common.context.TransContext;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.tc.conf.TransInfoConfiguration;
import com.ops.sc.tc.mgr.DefaultGlobalTransProcess;
import com.ops.sc.tc.service.CommonTransactionAspectService;
import com.ops.sc.tc.service.SponsorInitService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


//@Service("transactionAspectService")
public class DistributeTransactionAspectServiceImpl implements DistributeTransactionAspectService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributeTransactionAspectServiceImpl.class);

    private DefaultGlobalTransProcess transactionManager = DefaultGlobalTransProcess.getInstance();

    @Resource
    private TransInfoConfiguration transInfoConfiguration;

    @Autowired
    private SponsorInitService sponsorInitService;

    @Autowired
    private CommonTransactionAspectService commonTransactionAspectService;


    public Object transAdvise(ProceedingJoinPoint joinPoint, DistributeTrans distributeTrans)
            throws Throwable {
        String businessId = AnnotationParamUtils.getIdFromParam(joinPoint, distributeTrans.bizId());
        if (transInfoConfiguration.isServiceDisable()) {
            return joinPoint.proceed();
        }
        String transGroupId = distributeTrans.groupId();
        String appName = transInfoConfiguration.getAppName();
        TransMode transMode = distributeTrans.transMode();
        if (transMode != TransMode.TCC&&transMode != TransMode.XA) {
            throw new ScClientException(ClientErrorCode.UNSUPPORTED,
                    "DistributeTrans only support TCC or XA!");
        }
        if (!TransactionContextRecorder.isTidExist()) {
            TransactionContextRecorder.setCurrentTransContext(
                    new TransContext(businessId, getServerAddress()));
        }
        TransactionContextRecorder.setCurrentTransMode(transMode);
        try{
            return commonTransactionAspectService.startGlobalTrans(transGroupId, appName, distributeTrans, businessId);
        }
        finally {
            TransactionContextRecorder.clearAllTransContext();
        }
    }

    private String getServerAddress() throws ScClientException{
        List<String> list = sponsorInitService.getServerList();
        if(list!=null&& !list.isEmpty()) {
            return list.get(ThreadLocalRandom.current().nextInt(list.size()));
        }
        String msg = "no servers available!";
        throw new ScClientException(ClientErrorCode.SERVER_NOT_AVAILABLE, msg);
    }

    private Long startTrans(ProceedingJoinPoint pjp, DistributeTrans distributeTrans, String bizId, String transGroupId, String appName,TransMode transMode) throws ScClientException,SQLException {
        Long tid;
        tid = commonTransactionAspectService.startGlobalTrans(transGroupId, appName, pjp, distributeTrans, bizId);
        TransactionContextRecorder.setRoleContext(TransferRole.STARTER);
        TransactionContextRecorder.setCurrentTransContext(new TransContext(bizId, true, tid, tid));
        return tid;
    }



   /* private GlobalTransNotifyResponse getGlobalTransNotifyResponse(String tid, TransStatus status) throws RpcException {
        GlobalTransNotifyRequest.Builder reportGlobalTransRequestBuilder = GlobalTransNotifyRequest.newBuilder();
        reportGlobalTransRequestBuilder.setTid(tid);
        reportGlobalTransRequestBuilder.setStatus(UInt32Value.of(status.getValue()));
        GlobalTransNotifyResponse response = transactionManager.notifyGlobal(reportGlobalTransRequestBuilder.build());
        return response;
    }*/

    /*private void initImage(){
        if (ImageContextRecorder.get() == null) {
            ImageContextRecorder.init();
        }
    }*/

}
