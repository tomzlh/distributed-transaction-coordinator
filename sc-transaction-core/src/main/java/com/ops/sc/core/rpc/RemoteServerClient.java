package com.ops.sc.core.rpc;


import com.ops.sc.common.exception.ResourceException;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.rpc.grpc.RemoteCallRequest;
import com.ops.sc.rpc.grpc.RemoteCallServiceGrpc;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import io.grpc.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class RemoteServerClient {
    private static final RemoteServerClient INSTANCE = new RemoteServerClient();
    private int port;
    private Map<String, ManagedChannel> channelFactory = new ConcurrentHashMap<>();

    private RemoteServerClient() {
    }

    public static RemoteServerClient getInstance() {
        return INSTANCE;
    }

    public void init(int port) {
        this.port = port;
    }

    private Channel getChannel(String host) {
        if (!channelFactory.containsKey(host)) {
            synchronized (channelFactory) {
                if (!channelFactory.containsKey(host)) {
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
                    channelFactory.put(host, channel);
                }
            }
        }
        return channelFactory.get(host);
    }

    /**
     * 远程调用
     *
     * @param host
     * @param request
     * @param timeoutMills
     * @return
     * @throws RpcException
     *             timeout或其他异常
     * @throws ResourceException
     *             远程服务器没有对应的可用连接
     */
    public RpcCallBackRequest remoteCall(String host, RemoteCallRequest request, Long timeoutMills)
            throws RpcException, ResourceException {
        Channel channel = getChannel(host);

        try {
            RemoteCallServiceGrpc.RemoteCallServiceBlockingStub stub = RemoteCallServiceGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(timeoutMills, TimeUnit.MILLISECONDS);

            return stub.remoteCall(request);
        } catch (StatusRuntimeException e) {
            if (Status.RESOURCE_EXHAUSTED == e.getStatus()) {
                throw new ResourceException("remote server no available observer.");
            }

            throw new RpcException("call remote server fail: " + e.getMessage(), e);
        }

    }

}
