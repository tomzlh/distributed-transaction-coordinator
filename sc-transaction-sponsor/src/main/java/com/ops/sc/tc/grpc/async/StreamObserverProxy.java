package com.ops.sc.tc.grpc.async;


import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import io.grpc.stub.StreamObserver;


public class StreamObserverProxy implements StreamObserver<RpcCallBackRequest> {
    private StreamObserver<RpcCallBackRequest> streamObserver;

    public StreamObserverProxy(StreamObserver<RpcCallBackRequest> streamObserver) {
        this.streamObserver = streamObserver;
    }

    @Override
    public synchronized void onNext(RpcCallBackRequest rpcCallBackRequest) {
        streamObserver.onNext(rpcCallBackRequest);
    }

    @Override
    public void onError(Throwable throwable) {
        streamObserver.onError(throwable);
    }

    @Override
    public void onCompleted() {
        streamObserver.onCompleted();
    }
}

