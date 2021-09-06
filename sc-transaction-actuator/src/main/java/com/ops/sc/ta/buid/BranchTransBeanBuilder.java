package com.ops.sc.ta.buid;

import com.google.common.base.Strings;
import com.google.protobuf.UInt32Value;
import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.utils.InetUtil;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.GlobalTransRequest;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.ta.anno.BranchOperation;
import com.ops.sc.ta.anno.BranchRecovery;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


public class BranchTransBeanBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchTransBeanBuilder.class);


    public static BranchTransRequest buildLogicalBranchParams(String appName, ProceedingJoinPoint pjp,
                                                                 String branchName, Long timeout, TimeoutType timeoutType) throws ScClientException{
        BranchTransRequest.Builder requestBuilder = buildCommonParams(pjp, branchName, timeout,
                timeoutType.getValue(), appName);
        requestBuilder.setBranchType(UInt32Value.of(TransProcessMode.LOGIC_BRANCH.getValue()));
        //requestBuilder.setStatus(UInt32Value.of(TransStatus.TRY_SUCCEED.getValue()));
        Long tid = TransactionContextRecorder.getTid();
        requestBuilder.setTid(String.valueOf(tid));
        Long parentId = TransactionContextRecorder.getParentId();
        //requestBuilder.setParentId(String.valueOf(parentId));
        return requestBuilder.build();
    }


    public static <T> BranchTransRequest buildTccBranchRegisterParams(ProceedingJoinPoint pjp,
                                                                      T annotationObject, String appName) throws ScClientException {
        String name;
        long timeout;
        TimeoutType timeoutType;
        if (annotationObject instanceof BranchOperation) {
            BranchOperation branchOperation = (BranchOperation) annotationObject;
            name = branchOperation.name();
            timeout = branchOperation.timeout();
            timeoutType = branchOperation.timeoutType();
        } else if (annotationObject instanceof BranchRecovery) {
            BranchRecovery branchRecovery = (BranchRecovery) annotationObject;
            name = branchRecovery.name();
            timeout = branchRecovery.timeout();
            timeoutType = branchRecovery.timeoutType();
        } else {
            throw new UnsupportedOperationException("Unknown annotation type: " + annotationObject);
        }

        BranchTransRequest.Builder requestBuilder = buildCommonParams(pjp, name, timeout,
                timeoutType.getValue(), appName);
        requestBuilder.setBranchType(UInt32Value.of(TransMode.TCC.getValue()));
        requestBuilder.setTid(String.valueOf(TransactionContextRecorder.getTid()));
        return requestBuilder.build();
    }




    private static BranchTransRequest.Builder buildCommonParams(ProceedingJoinPoint pjp, String branchName,
                                                                   long timeout, Integer timeoutStrategyValue, String appName) throws ScClientException{
        BranchTransRequest.Builder requestBuilder = BranchTransRequest.newBuilder();
        requestBuilder.setCallerIp(InetUtil.getHostIp());
        //requestBuilder.setTransactionName(branchName);
        //requestBuilder.setTimeout(UInt64Value.of(timeout));
        //requestBuilder.setTimeoutStrategy(UInt32Value.of(timeoutStrategyValue));
        if (Strings.isNullOrEmpty(appName)) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, "AppName cannot be null");
        }
        requestBuilder.setBranchTransName(appName);
        return requestBuilder;
    }


    public static BranchTransRequest globalParams2XABranchParams(
            final GlobalTransRequest registerGlobalRequest) {
        BranchTransRequest.Builder builder = globalParams2BranchBaseParams(registerGlobalRequest);
        builder.setBranchType(UInt32Value.of(TransMode.FMT.getValue()));
        return builder.build();
    }



    public static BranchTransRequest globalParams2FMTBranchParams(
            final GlobalTransRequest registerGlobalRequest) {
        BranchTransRequest.Builder builder = globalParams2BranchBaseParams(registerGlobalRequest);
        builder.setBranchType(UInt32Value.of(TransMode.FMT.getValue()));
        return builder.build();

    }


    private static BranchTransRequest.Builder globalParams2BranchBaseParams(
            final GlobalTransRequest globalTransRequest) {
        BranchTransRequest.Builder requestBuilder = BranchTransRequest.newBuilder();
        requestBuilder.setBranchTransName(globalTransRequest.getAppName());
        //requestBuilder.setBranchName(globalTransRequest.getTransName());
        //requestBuilder.setInstanceName(globalTransRequest.getInstanceName());
        requestBuilder.setTid(String.valueOf(TransactionContextRecorder.getTid()));

        requestBuilder.setCallerIp(globalTransRequest.getCallerIp());
        Long tid = TransactionContextRecorder.getTid();
        requestBuilder.setTid(String.valueOf(tid));
        Long parentId = TransactionContextRecorder.getParentId();
        return requestBuilder;
    }

    private static String getInstanceName(ProceedingJoinPoint pjp) {
        String instanceNameInterface = pjp.getTarget().getClass().getCanonicalName();
        Class[] interfaces = pjp.getTarget().getClass().getInterfaces();
        if (interfaces.length == 1) {
            instanceNameInterface = interfaces[0].getCanonicalName();
        }
        return instanceNameInterface + "." + pjp.getSignature().getName();
    }

    public static String getTagId(String appName, String methodName) {
        return appName + "." + methodName;
    }

    private static boolean isCollectionClassType(Class clazz) {
        if (clazz.isAssignableFrom(List.class) || clazz.isAssignableFrom(Set.class) || clazz.isAssignableFrom(Map.class)
                || clazz.isAssignableFrom(Queue.class)) {
            return true;
        }
        return false;
    }
}
