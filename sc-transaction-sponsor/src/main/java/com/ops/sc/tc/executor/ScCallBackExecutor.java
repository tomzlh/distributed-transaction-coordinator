package com.ops.sc.tc.executor;

import com.ops.sc.common.enums.*;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.sql.SQLException;

public class ScCallBackExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScCallBackExecutor.class);



    private ScCallBackExecutor() {
    }

    public static ScCallBackExecutor getInstance() {
        return CallBackExecutorHolder.EXECUTOR;
    }

    public void handleCallBack(RpcCallBackResponse rpcCallBackResponse,
                               StreamObserver<RpcCallBackRequest> requestStreamObserver) {
        RpcCallBackRequest.Builder builder = RpcCallBackRequest.newBuilder()
                .setRequestId(rpcCallBackResponse.getRequestId());
        try {
            /*if (CallBackType.CHECKBACK.getValue() == rpcCallBackResponse.getCallBackType()) {
                GlobalTransStatus checkBackResult = getTransactionInitiator()
                        .checkBack(Long.parseLong(rpcCallBackResponse.getTid()), rpcCallBackResponse.getDataSource());
                builder.setCheckBackResult(checkBackResult.name());
                requestStreamObserver.onNext(builder.build());
                return;
            }

            // 本地模式RmCompensate
            if (CallBackType.LOCAL_COMPENSATE.getValue() == rpcCallBackResponse.getCallBackType()) {
                LocalCompensateManager.getInstance().compensate();
                requestStreamObserver.onNext(builder.build());
                return;
            }

            // 本地模式手动运维重试失败的分支
            if (CallBackType.RETRY.getValue() == rpcCallBackResponse.getCallBackType()) {
                LocalCompensateManager.getInstance().retryBranch(Long.parseLong(rpcCallBackResponse.getTid()),
                        rpcCallBackResponse.getBranchId());
                requestStreamObserver.onNext(builder.build());
                return;
            }

            // 本地模式手动运维回滚超时的分支
            if (CallBackType.CANCEL_TIMEOUT_BRANCH.getValue() == rpcCallBackResponse.getCallBackType()) {
                LocalCompensateManager.getInstance().cancelTimeoutBranch(Long.parseLong(rpcCallBackResponse.getTid()),
                        rpcCallBackResponse.getBranchId());
                requestStreamObserver.onNext(builder.build());
                return;
            }


            if (TransProcessMode.TCC.getValue() == rpcCallBackResponse.getBranchType()) {
                RpcCallBackRequest rpcCallBackRequest = TccTaCallIn.getInstance()
                        .handleCallBack(rpcCallBackResponse);
                builder = rpcCallBackRequest.toBuilder();
            }
            if (TransProcessMode.FMT.getValue() == rpcCallBackResponse.getBranchType()) {
                RpcCallBackRequest rpcCallBackRequest = getFmtResourceManagerInBound()
                        .handleCallBack(rpcCallBackResponse);
                builder = rpcCallBackRequest.toBuilder();
            }
            if (TransProcessMode.MQ_LOCAL.getValue() == rpcCallBackResponse.getBranchType()
                    || TransProcessMode.MQ_NATIVE_LOCAL.getValue() == rpcCallBackResponse.getBranchType()) {
                RpcCallBackRequest rpcCallBackRequest = MqTaCallIn.getInstance()
                        .handleCallBack(rpcCallBackResponse);
                builder = rpcCallBackRequest.toBuilder();
            }

            if (TransMode.XA.getValue() == rpcCallBackResponse.getBranchType()) {
                RpcCallBackRequest rpcCallBackRequest = getXaResourceManagerInBound()
                        .handleCallBack(rpcCallBackResponse);
                builder = rpcCallBackRequest.toBuilder();
            }*/
        } catch (NoSuchBeanDefinitionException e) {
            builder.setCode(CallErrorCode.NO_DATASOURCE.getValue());
        } catch (Exception e) {
            LOGGER.debug("Callback failed, tid : {}, branchId : {}. ", rpcCallBackResponse.getTid(),
                    rpcCallBackResponse.getBranchId(), e);
            builder.setCode(CallErrorCode.UNKNOWN_EXCEPTION.getValue());
        }
        requestStreamObserver.onNext(builder.build());
    }



   /* private TransactionInitiator getTransactionInitiator() {
        if (this.transactionInitiator == null) {
            this.transactionInitiator = ApplicationUtils.getBean("transactionInitiator",
                    TransactionInitiator.class);
        }
        return this.transactionInitiator;
    }

    private FmtTaCallIn getFmtResourceManagerInBound() {
        if (this.fmtResourceManagerInBound == null) {
            this.fmtResourceManagerInBound = ApplicationUtils.getBean(FmtTaCallIn.class);
        }
        return this.fmtResourceManagerInBound;
    }

    private XARMCallIn getXaResourceManagerInBound() {
        if (this.xaResourceManagerInBound == null) {
            this.xaResourceManagerInBound = ApplicationUtils.getBean(XARMCallIn.class);
        }
        return this.xaResourceManagerInBound;
    }*/

    private static class CallBackExecutorHolder {
        private static final ScCallBackExecutor EXECUTOR = new ScCallBackExecutor();
    }

}
