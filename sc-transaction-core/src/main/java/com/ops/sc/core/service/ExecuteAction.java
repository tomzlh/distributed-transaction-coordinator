package com.ops.sc.core.service;

import com.ops.sc.common.exception.ResourceException;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;
import io.grpc.stub.StreamObserver;

import java.util.Optional;


public interface ExecuteAction {

    /**
     * 代理分支事务/全局事务回查，采用异步方式，将结果返回给serverObserver
     *
     * @param response
     * @throws ResourceException
     *             不存在本地缓存的客户端长连接
     */
    void callbackProxy(RpcCallBackResponse response, String appName, String uniqueId,
                       StreamObserver<RpcCallBackRequest> serverObserver) throws ResourceException;

    /**
     * 执行二阶段回调 采用本地同步调用的方式返回
     *
     * @return executeSuccess/executeFail
     * @throws ResourceException
     *             不存在本地缓存的客户端长连接
     */
    Optional<RpcCallBackRequest> executionCallback(RpcCallBackResponse response, String appName)
            throws ResourceException;

}
