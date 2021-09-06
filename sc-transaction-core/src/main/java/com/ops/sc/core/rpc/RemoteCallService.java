package com.ops.sc.core.rpc;

import com.ops.sc.common.exception.ResourceException;
import com.ops.sc.core.util.ApplicationUtils;
import com.ops.sc.rpc.grpc.RemoteCallRequest;
import com.ops.sc.rpc.grpc.RemoteCallServiceGrpc;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.core.service.ExecuteAction;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RemoteCallService extends RemoteCallServiceGrpc.RemoteCallServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteCallService.class);

    private ExecuteAction executeCallBackService = ApplicationUtils.getBean(ExecuteAction.class);

    @Override
    public void remoteCall(RemoteCallRequest request, StreamObserver<RpcCallBackRequest> responseObserver) {
        try {
            LOGGER.debug("Receive inner sc server request : {}", request.toString());
            executeCallBackService.callbackProxy(request.getRpcCallBackResponse(), request.getAppName(),
                    request.getUid(), responseObserver);
        } catch (ResourceException e) {
            LOGGER.warn("No available connection for appName : {}, uniqueId : {} ", request.getAppName(),
                    request.getUid());
            responseObserver.onError(Status.RESOURCE_EXHAUSTED.asException());
        } catch (Exception e) {
            LOGGER.error("Remote call failed. ", e);
            responseObserver.onError(Status.INTERNAL.asException());
        }
    }
}
