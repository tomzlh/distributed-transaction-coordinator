package com.ops.sc.server.service.impl;


import com.google.common.base.Preconditions;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.CallErrorCode;
import com.ops.sc.common.enums.CallBackType;
import com.ops.sc.common.enums.ExecutionResult;
import com.ops.sc.common.enums.GlobalTransStatus;
import com.ops.sc.common.exception.ResourceException;
import com.ops.sc.common.model.TransactionInfo;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.utils.InetUtil;
import com.ops.sc.common.utils.StringTools;
import com.ops.sc.common.utils.UUIDGenerator;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.common.reg.zk.ZookeeperRegistryCenter;
import com.ops.sc.core.service.ExecuteAction;
import com.ops.sc.common.utils.CommonUtils;
import com.ops.sc.rpc.grpc.RemoteCallRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;
import com.ops.sc.core.rpc.RemoteServerClient;
import com.ops.sc.server.service.CallAction;
import com.ops.sc.server.service.CallBackErrorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


@Service
public class CallActionServiceImpl implements CallAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallActionServiceImpl.class);

    @Resource
    private ExecuteAction executeCallBackService;

    @Resource
    private CallBackErrorService callBackErrorService;

    @Override
    public GlobalTransStatus globalCheckBackExecute(ScTransRecord transactionInfo) {
        RpcCallBackResponse response = buildGlobalCheckBackExecuteRequest(transactionInfo);
        try {
            Optional<RpcCallBackRequest> request = executeCallBackService.executionCallback(response,
                    transactionInfo.getAppName());
            return handleGlobalCheckRpcRequest(request, Long.parseLong(response.getTid()));
        } catch (ResourceException e) {
            try {
                RpcCallBackRequest request = remoteCallback(response, transactionInfo.getAppName());
                return handleGlobalCheckRpcRequest(Optional.ofNullable(request), Long.parseLong(response.getTid()));

            } catch (ResourceException ex) {
                LOGGER.error(
                        "Remote server not have available connection. tid : {},  check back transaction status fail, because ",
                        transactionInfo.getTid());
                callBackErrorService.recordCallBackErrorInfoAsync(transactionInfo.getTid(),
                        CallErrorCode.NO_AVAILABLE_CONNECTION);
            } catch (RpcException ex) {
                LOGGER.error("Call remote server catch exception. tid : {} check back transaction status fail, exception: {}",
                        transactionInfo.getTid(), ex);
                callBackErrorService.recordCallBackErrorInfoAsync(transactionInfo.getTid(), CallErrorCode.RPC_EXCEPTION, ex);
            }
            return GlobalTransStatus.TRYING;
        }
    }

    private GlobalTransStatus handleGlobalCheckRpcRequest(Optional<RpcCallBackRequest> rpcCallBackRequest,
            Long tid) {
        if (!rpcCallBackRequest.isPresent()) {
            callBackErrorService.recordCallBackErrorInfoAsync(tid, CallErrorCode.TIMEOUT_EXCEPTION);
            return GlobalTransStatus.TRYING;
        }
        if (CallErrorCode.getCallBackErrorByCodeNum(rpcCallBackRequest.get().getCode()).isSuccess()) {
            // 请求正常完成
            return GlobalTransStatus.getCheckBackResultEnumByName(rpcCallBackRequest.get().getCheckBackResult());
        } else {
            // code != 0
            callBackErrorService.recordCallBackErrorInfoAsync(tid,
                    CallErrorCode.getCallBackErrorByCodeNum(rpcCallBackRequest.get().getCode()));
            return GlobalTransStatus.TRYING;
        }
    }

    /**
     * 全局事务上报成功后通知客户端删除sc_log记录，使用common线程池异步执行即可
     *
     * @param transactionInfo
     */
    @Async("commonTask")
    @Override
    public void notifyClient(TransactionInfo transactionInfo) {
        RpcCallBackResponse response = buildRequestToNotifyClient(transactionInfo);
        try {
            Optional<RpcCallBackRequest> request = executeCallBackService.executionCallback(response,
                    transactionInfo.getAppName());
            notifyClientToDeleteLogRpcRequest(request, Long.parseLong(response.getTid()));
        } catch (ResourceException e) {
            try {
                RpcCallBackRequest request = remoteCallback(response, transactionInfo.getAppName());
                notifyClientToDeleteLogRpcRequest(Optional.ofNullable(request), Long.parseLong(response.getTid()));
            } catch (ResourceException | RpcException ex) {
                LOGGER.error("Notify client to delete log failed. tid: {} ", transactionInfo.getTid(), ex);
            }
        }
    }

    private void notifyClientToDeleteLogRpcRequest(Optional<RpcCallBackRequest> request, Long tid) {
        if (!request.isPresent()) {
            LOGGER.warn("Notify client to delete log timeout. tid: {}", tid);
            return;
        }
        if (!CallErrorCode.getCallBackErrorByCodeNum(request.get().getCode()).isSuccess()) {
            CallErrorCode callErrorCode = CallErrorCode.getCallBackErrorByCodeNum(request.get().getCode());
            LOGGER.warn("Notify client to delete scLog error: {} tid: {}", callErrorCode, tid);
            return;
        }
        LOGGER.debug("Notify client to delete scLog success. tid: {}", tid);
    }

    private RpcCallBackResponse buildRequestToNotifyClient(TransactionInfo transactionInfo) {
        String requestId = UUIDGenerator.generateUUID();
        return RpcCallBackResponse.newBuilder().setTid(String.valueOf(transactionInfo.getTid()))
                .setCallBackType(CallBackType.DELETE_LOG.getValue()).setRequestId(requestId).build();
    }

    /**
     * 调用远程服务端进行回调
     *
     * @param response
     * @param appName
     * @return
     * @throws ResourceException
     *             远程服务端没有连接
     * @throws RpcException
     *             其他异常
     */
    private RpcCallBackRequest remoteCallback(RpcCallBackResponse response, String appName)
            throws ResourceException, RpcException {
        //Map<String, String> availableConnectionMap = etcdService.getAllValueByPrefix(EtcdConstants.PREFIX + appName);
        List<String> availableConnections;
        try {
            availableConnections = ZookeeperRegistryCenter.getInstance().getClient().getChildren().forPath(ZookeeperRegistryCenter.PREFIX + appName);
        }catch (Exception e){
            throw new ResourceException("Get available observer in local and remote server error.",e);
        }
        if (availableConnections.isEmpty()) {
            LOGGER.error("No available observer in local and remote server. xid: {}, branchId : {}, callback fail, ",
                    response.getTid(), response.getBranchId());
            throw new ResourceException("No available observer in local and remote server.");
        }
        RemoteCallRequest.Builder builder = RemoteCallRequest.newBuilder();
        builder.setAppName(appName);
        builder.setRpcCallBackResponse(response);
        String localIp = InetUtil.getHostIp();
        long end = Constants.DEFAULT_TIMEOUT + System.currentTimeMillis();
        for (String remoteIp : availableConnections) {
            if (remoteIp.equals(localIp)) {
                continue;
            }
            Long timeout = end - System.currentTimeMillis();
            if (timeout <= 0) {
                throw new RpcException("remote call timeout");
            } else {
                builder.setUid(StringTools.getUniqueIdOnly(appName));
                try {
                    return RemoteServerClient.getInstance().remoteCall(remoteIp, builder.build(), timeout);
                } catch (ResourceException ex) {
                    LOGGER.warn("remote server : {} do not have connection about appName : {}, uniqueId : {}", remoteIp,
                            appName, builder.getUid());
                }
            }
        }
        throw new ResourceException("No remote available observer.");
    }

    @Override
    public ExecutionResult branchExecute(ScBranchRecord transBranchInfo, CallBackType callBackType) {
        RpcCallBackResponse response = buildBranchExecuteRequest(transBranchInfo, callBackType);
        try {
            Optional<RpcCallBackRequest> request = executeCallBackService.executionCallback(response,
                    transBranchInfo.getBranchTransName());
            return handleBranchRpcRequest(request, Long.parseLong(response.getTid()), Long.parseLong(response.getBranchId()));
        } catch (ResourceException e) {
            try {
                RpcCallBackRequest request = remoteCallback(response, transBranchInfo.getBranchTransName());
                return handleBranchRpcRequest(Optional.ofNullable(request), Long.parseLong(response.getTid()), Long.parseLong(response.getBranchId()));
            } catch (ResourceException ex) {
                LOGGER.error("Remote server not have available connection. tid : {}, branchId : {} execute fail ",
                        transBranchInfo.getTid(), transBranchInfo.getBid());
                callBackErrorService.recordCallBackErrorInfoAsync(transBranchInfo.getTid(), transBranchInfo.getBid(),
                        CallErrorCode.NO_AVAILABLE_CONNECTION);
            } catch (RpcException ex) {
                LOGGER.error(
                        "Call remote server catch exception. xid : {}, branchId : {} execute fail , exception : {}",
                        transBranchInfo.getTid(), transBranchInfo.getBid(), ex);
                callBackErrorService.recordCallBackErrorInfoAsync(transBranchInfo.getTid(), transBranchInfo.getBid(),
                        CallErrorCode.RPC_EXCEPTION, ex);
            }
            return ExecutionResult.FAILED;
        }
    }

    /**
     * 本地模式手动运维重试分支Callback
     *
     * @param tid
     * @param branchId
     * @param appName
     * @return
     */
    @Override
    public boolean retryFailLocalBranch(Long tid, String branchId, String appName) {
        return handleAbnormalLocalBranch(tid, branchId, appName, CallBackType.RETRY);
    }

    /**
     * 本地模式手动运维Callback
     *
     * @param tid
     * @param branchId
     * @param appName
     * @return
     */
    @Override
    public boolean cancelTimeoutLocalBranch(Long tid, String branchId, String appName) {
        return handleAbnormalLocalBranch(tid, branchId, appName, CallBackType.CANCEL_TIMEOUT_BRANCH);
    }

    /**
     * 通知客户端进行运维操作
     *
     * @param tid
     * @param branchId
     * @param appName
     * @param opsType
     * @return
     */
    private boolean handleAbnormalLocalBranch(Long tid, String branchId, String appName, CallBackType opsType) {
        Preconditions.checkNotNull(appName, "AppName cannot be null for local ops!");
        RpcCallBackResponse response = RpcCallBackResponse.newBuilder().setTid(String.valueOf(tid)).setBranchId(branchId)
                .setCallBackType(opsType.getValue()).build();
        LOGGER.info("Local ops start. tid: {} branchId: {} appName: {} opsType: {}", tid, branchId, appName, opsType);
        try {
            Optional<RpcCallBackRequest> request = executeCallBackService.executionCallback(response, appName);
            return handleLocalOpsResponse(request, appName);
        } catch (ResourceException ex) {
            try {
                RpcCallBackRequest request = remoteCallback(response, appName);
                return handleLocalOpsResponse(Optional.ofNullable(request), appName);
            } catch (ResourceException e) {
                LOGGER.error("Remote server not have available connection. local ops fail appName: {}", appName, e);
            } catch (RpcException e) {
                LOGGER.error("Call remote server catch exception. local ops fail, appName: {}", appName, e);
            }
        }
        return false;
    }

    private boolean handleLocalOpsResponse(Optional<RpcCallBackRequest> request, String appName) {
        if (!request.isPresent()) {
            LOGGER.error("Local ops callBack timeout. appName: {}", appName);
            return false;
        }
        if (!CallErrorCode.getCallBackErrorByCodeNum(request.get().getCode()).isSuccess()) {
            CallErrorCode callErrorCode = CallErrorCode.getCallBackErrorByCodeNum(request.get().getCode());
            LOGGER.error("Local ops callback error: {} appName: {}", callErrorCode, appName);
            return false;
        }
        LOGGER.info("Local ops callback rpc success. appName: {}", appName);
        return true;
    }

    /**
     * 同库模式下通知客户端处理异常事务
     */
    @Override
    public void localCompensate() throws ResourceException{
        List<String> availableConnections;
        try {
            availableConnections = ZookeeperRegistryCenter.getInstance().getClient().getChildren().forPath(ZookeeperRegistryCenter.PREFIX+ZookeeperRegistryCenter.LOCAL_MODE_SUFFIX);
        }catch (Exception e){
            throw new ResourceException("Get available observer in local and remote server error.",e);
        }
        Set<String> localAppNameSet = new TreeSet<>();
        availableConnections.forEach(value -> {
                localAppNameSet.add(StringTools.getAppNameOnly(value));
        });
        if (localAppNameSet.size() > 0) {
            localAppNameSet.forEach(this::handleLocalCompensate);
        }
    }

    private void handleLocalCompensate(String appName) {
        LOGGER.debug("Callback client to do rmCompensate. appName: {}", appName);
        Preconditions.checkNotNull(appName, "AppName cannot be null for local rmCompensate!");
        RpcCallBackResponse response = buildLocalCompensateRequest();
        try {
            Optional<RpcCallBackRequest> request = executeCallBackService.executionCallback(response, appName);
            handleLocalCompensateResponse(request, appName);
        } catch (ResourceException ex) {
            try {
                RpcCallBackRequest request = remoteCallback(response, appName);
                handleLocalCompensateResponse(Optional.ofNullable(request), appName);
                return;
            } catch (ResourceException e) {
                LOGGER.error("Remote server not have available connection. rmCompensate fail appName: {}", appName, e);
            } catch (RpcException e) {
                LOGGER.error("Call remote server catch exception. rmCompensate fail, appName: {}", appName, e);
            }
        }
        LOGGER.debug("Local rmCompensate callback success. appName: {}", appName);
    }

    private void handleLocalCompensateResponse(Optional<RpcCallBackRequest> request, String appName) {
        if (!request.isPresent()) {
            LOGGER.error("Local rmCompensate callBack timeout. appName: {}", appName);
            return;
        }
        if (!CallErrorCode.getCallBackErrorByCodeNum(request.get().getCode()).isSuccess()) {
            CallErrorCode callErrorCode = CallErrorCode.getCallBackErrorByCodeNum(request.get().getCode());
            LOGGER.error("Local rmCompensate callback error: {} appName: {}", callErrorCode, appName);
        }
    }

    private ExecutionResult handleBranchRpcRequest(Optional<RpcCallBackRequest> rpcCallBackRequest, Long tid,
                                                   Long branchId) {
        if (!rpcCallBackRequest.isPresent()) {
            callBackErrorService.recordCallBackErrorInfoAsync(tid, branchId, CallErrorCode.TIMEOUT_EXCEPTION);
            return ExecutionResult.FAILED;
        }
        if (CallErrorCode.getCallBackErrorByCodeNum(rpcCallBackRequest.get().getCode()).isSuccess()) {
            // 请求正常完成
            if (rpcCallBackRequest.get().getTwoPCResult()) {
                return ExecutionResult.SUCCEED;
            } else {
                callBackErrorService.recordCallBackErrorInfoAsync(tid, branchId, CallErrorCode.CALLBACK_EXECUTE_FAILED);
                return ExecutionResult.FAILED;
            }
        } else {
            // code != 0
            CallErrorCode callErrorCode = CallErrorCode.getCallBackErrorByCodeNum(rpcCallBackRequest.get().getCode());
            callBackErrorService.recordCallBackErrorInfoAsync(tid, branchId, callErrorCode);
            return ExecutionResult.FAILED;
        }
    }

    private RpcCallBackResponse buildBranchExecuteRequest(ScBranchRecord transBranchInfo, CallBackType callBackType) {
        String requestId = UUIDGenerator.generateUUID();
        String appName = transBranchInfo.getBranchTransName();
        Preconditions.checkNotNull(appName, "AppName cannot be null for branch execute!");
        RpcCallBackResponse.Builder builder = RpcCallBackResponse.newBuilder().setTid(String.valueOf(transBranchInfo.getTid()))
                .setBranchId(String.valueOf(transBranchInfo.getBid())).setBranchType(transBranchInfo.getTransMode())
                .setDataSource(transBranchInfo.getDataSource()).setParams(CommonUtils.toByteString(transBranchInfo.getBranchParam()))
                .setRequestId(requestId).setCallBackType(callBackType.getValue());
        return builder.build();
    }

    /**
     * 本地模式
     *
     * @return
     */
    private RpcCallBackResponse buildLocalCompensateRequest() {
        String requestId = UUIDGenerator.generateUUID();
        return RpcCallBackResponse.newBuilder().setCallBackType(CallBackType.LOCAL_COMPENSATE.getValue())
                .setRequestId(requestId).build();
    }

    private RpcCallBackResponse buildGlobalCheckBackExecuteRequest(ScTransRecord transactionInfo) {
        String requestId = UUIDGenerator.generateUUID();
        String appName = transactionInfo.getAppName();
        Preconditions.checkNotNull(appName, "AppName cannot be null for global checkBack!");
        return RpcCallBackResponse.newBuilder().setTid(String.valueOf(transactionInfo.getTid()))
                .setCallBackType(CallBackType.CHECKBACK.getValue()).setDataSource(transactionInfo.getDataSource())
                .setRequestId(requestId).build();
    }
}
