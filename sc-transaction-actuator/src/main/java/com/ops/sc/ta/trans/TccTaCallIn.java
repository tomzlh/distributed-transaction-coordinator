package com.ops.sc.ta.trans;


import com.google.common.collect.Maps;
import com.ops.sc.common.enums.CallErrorCode;
import com.ops.sc.common.enums.CallBackType;
import com.ops.sc.common.enums.LocalInvokeType;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.core.service.tcc.TccExecuteInfo;
import com.ops.sc.common.utils.CommonUtils;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;


public class TccTaCallIn implements TaCallIn {

    private static final Logger LOGGER = LoggerFactory.getLogger(TccTaCallIn.class);

    private Map<String, TccExecuteInfo> tccInfoCache = Maps.newHashMap();

    private TccTaCallIn() {
    }

    public static TccTaCallIn getInstance() {
        return TccResourceManagerHolder.TCC_RESOURCE_MANAGER;
    }

    public void register(TccExecuteInfo tccExecuteInfo) {
        tccInfoCache.put(tccExecuteInfo.getTagId(), tccExecuteInfo);
    }

    public RpcCallBackRequest handleCallBack(RpcCallBackResponse rpcCallBackResponse) throws ScClientException{

        if (CallBackType.COMMIT.getValue() == rpcCallBackResponse.getCallBackType()) {
            return this.commitBranch(rpcCallBackResponse);
        }
        if (CallBackType.ROLLBACK.getValue() == rpcCallBackResponse.getCallBackType()) {
            return this.rollBackBranch(rpcCallBackResponse);
        }
        throw new UnsupportedOperationException("not supported callBackType!");
    }

    @Override
    public RpcCallBackRequest prepare(RpcCallBackResponse rpcCallBackResponse) throws ScClientException, SQLException {
        return null;
    }

    @Override
    public RpcCallBackRequest commitBranch(RpcCallBackResponse rpcCallBackResponse) throws ScClientException {
        String resourceId = rpcCallBackResponse.getResourceId();
        TccExecuteInfo tccExecuteInfo = tccInfoCache.get(resourceId);
        LOGGER.debug("Tcc execute info : {}", tccExecuteInfo);
        RpcCallBackRequest.Builder builder = RpcCallBackRequest.newBuilder()
                .setRequestId(rpcCallBackResponse.getRequestId());
        if (tccExecuteInfo == null) {
            LOGGER.warn("Tcc execute info not exist. resourceId: {},branchId: {}", resourceId,
                    rpcCallBackResponse.getBranchId());
            builder.setCode(CallErrorCode.REFLECT_FAILED.getValue());
            return builder.build();
        }

        try {
            Boolean result = invoke(CommonUtils.toObjectArray(rpcCallBackResponse.getParams()),
                    tccExecuteInfo.getTargetBean(), tccExecuteInfo.getConfirmMethod());
            builder.setTwoPCResult(result);
        } catch (InvocationTargetException e) {
            LOGGER.error("Invoke target failed. resourceId: {}, branchId: {}", resourceId,
                    rpcCallBackResponse.getBranchId(), e);
            builder.setCode(CallErrorCode.INVOCATION_TARGET_EXCEPTION.getValue());
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Reflect failed. resourceId: {}, branchId: {}", resourceId, rpcCallBackResponse.getBranchId(),
                    e);
            builder.setCode(CallErrorCode.REFLECT_FAILED.getValue());
        }
        return builder.build();
    }

    @Override
    public RpcCallBackRequest rollBackBranch(RpcCallBackResponse rpcCallBackResponse) throws ScClientException{
        String resourceId = rpcCallBackResponse.getResourceId();
        TccExecuteInfo tccExecuteInfo = tccInfoCache.get(resourceId);
        LOGGER.debug("Tcc execute info : {}", tccExecuteInfo);

        RpcCallBackRequest.Builder builder = RpcCallBackRequest.newBuilder()
                .setRequestId(rpcCallBackResponse.getRequestId());
        try {
            Boolean result = invoke(CommonUtils.toObjectArray(rpcCallBackResponse.getParams()),
                    tccExecuteInfo.getTargetBean(), tccExecuteInfo.getCancelMethod());
            builder.setTwoPCResult(result);
        } catch (InvocationTargetException e) {
            LOGGER.debug("tid : {}, branchId : {} callback InvocationTargetException exception : ",
                    rpcCallBackResponse.getTid(), rpcCallBackResponse.getBranchId(), e);
            builder.setCode(CallErrorCode.INVOCATION_TARGET_EXCEPTION.getValue());
        } catch (ReflectiveOperationException e) {
            LOGGER.debug("tid : {}, branchId : {} callback reflect exception : ", rpcCallBackResponse.getTid(),
                    rpcCallBackResponse.getBranchId(), e);
            builder.setCode(CallErrorCode.REFLECT_FAILED.getValue());
        }
        return builder.build();
    }


    public Boolean localInvoke(Long tid, Long branchId, String resourceId, byte[] bytes,
            LocalInvokeType invokeType) throws ScClientException {
        TccExecuteInfo tccExecuteInfo = tccInfoCache.get(resourceId);
        Method method = LocalInvokeType.COMMIT == invokeType ? tccExecuteInfo.getConfirmMethod()
                : tccExecuteInfo.getCancelMethod();
        Boolean result;
        try {
            result = invoke(CommonUtils.toObjectArray(bytes), tccExecuteInfo.getTargetBean(), method);
        } catch (InvocationTargetException e) {
            LOGGER.debug("tid : {}, branchId : {} InvocationTargetException exception: ", tid, branchId, e);
            result = false;
        } catch (ReflectiveOperationException e) {
            result = false;
            LOGGER.debug("tid : {}, branchId : {} reflect exception: ", tid, branchId, e);
        }
        return result;
    }


    private Boolean invoke(Object[] objects, Object targetBean, Method callbackMethod)
            throws ReflectiveOperationException {
        try {
            if (callbackMethod == null) {
                return true;
            }
            Object result = callbackMethod.invoke(targetBean, objects);

            if (result == null) {
                throw new NoSuchMethodException(callbackMethod.getName());
            }

            if (!(result instanceof Boolean)) {
                throw new IllegalArgumentException("Method invoke result type is not Boolean");
            }

            return (Boolean) result;

        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                throw e;
            }
            throw new ReflectiveOperationException(e);
        }
    }

    private static class TccResourceManagerHolder {
        private static final TccTaCallIn TCC_RESOURCE_MANAGER = new TccTaCallIn();
    }
}
