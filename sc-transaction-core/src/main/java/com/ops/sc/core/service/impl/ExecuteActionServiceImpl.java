package com.ops.sc.core.service.impl;


import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.exception.ResourceException;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;
import com.ops.sc.core.rpc.CallBackStreamExecutors;
import com.ops.sc.core.rpc.RpcCallBackService;
import com.ops.sc.core.service.ExecuteAction;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class ExecuteActionServiceImpl implements ExecuteAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteActionServiceImpl.class);

    @Override
    public Optional<RpcCallBackRequest> executionCallback(RpcCallBackResponse response, String appName)
            throws ResourceException {

        StreamObserver<RpcCallBackResponse> localAvailableObserver = RpcCallBackService
                .getLocalAvailableStreamObserver(appName);
        if (localAvailableObserver == null) {
            LOGGER.debug("No local available observer. AppName: {}", appName);
            throw new ResourceException("No local available observer.");
        }

        String requestId = response.getRequestId();
        final CountDownLatch finishLatch = new CountDownLatch(1);
        RpcCallBackService.setObserverLatchMap(requestId, finishLatch);

        CallBackStreamExecutors.getInstance().callback(localAvailableObserver, response);

        try {
            finishLatch.await(Constants.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("finishLatch interruptedException.", e);
            return Optional.ofNullable(null);
        }
        RpcCallBackRequest rpcCallBackRequest = RpcCallBackService.getRpcCallBackRequest(requestId);
        RpcCallBackService.mapClear(requestId);
        return Optional.ofNullable(rpcCallBackRequest);
    }

    @Override
    public void callbackProxy(RpcCallBackResponse response, String appName, String uniqueId,
            StreamObserver<RpcCallBackRequest> serverObserver) throws ResourceException {
        StreamObserver<RpcCallBackResponse> localAvailableObserver = RpcCallBackService
                .getLocalAvailableStreamObserver(appName, uniqueId);
        if (localAvailableObserver == null) {
            throw new ResourceException("No local available observer.");
        }

        RpcCallBackService.setRemoteServerObserverMap(response.getRequestId(), serverObserver);

        CallBackStreamExecutors.getInstance().callback(localAvailableObserver, response);
    }
}
