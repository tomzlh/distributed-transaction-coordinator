package com.ops.sc.rpc.grpc;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.0.0)",
    comments = "Source: remoteCallService.proto")
public class RemoteCallServiceGrpc {

  private RemoteCallServiceGrpc() {}

  public static final String SERVICE_NAME = "RemoteCallService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.grpc.RemoteCallRequest,
      com.ops.sc.rpc.grpc.callback.RpcCallBackRequest> METHOD_REMOTE_CALL =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "RemoteCallService", "remoteCall"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.grpc.RemoteCallRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.grpc.callback.RpcCallBackRequest.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RemoteCallServiceStub newStub(io.grpc.Channel channel) {
    return new RemoteCallServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RemoteCallServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new RemoteCallServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static RemoteCallServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new RemoteCallServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class RemoteCallServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void remoteCall(com.ops.sc.rpc.grpc.RemoteCallRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.grpc.callback.RpcCallBackRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_REMOTE_CALL, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_REMOTE_CALL,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.grpc.RemoteCallRequest,
                com.ops.sc.rpc.grpc.callback.RpcCallBackRequest>(
                  this, METHODID_REMOTE_CALL)))
          .build();
    }
  }

  /**
   */
  public static final class RemoteCallServiceStub extends io.grpc.stub.AbstractStub<RemoteCallServiceStub> {
    private RemoteCallServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RemoteCallServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RemoteCallServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RemoteCallServiceStub(channel, callOptions);
    }

    /**
     */
    public void remoteCall(com.ops.sc.rpc.grpc.RemoteCallRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.grpc.callback.RpcCallBackRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REMOTE_CALL, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class RemoteCallServiceBlockingStub extends io.grpc.stub.AbstractStub<RemoteCallServiceBlockingStub> {
    private RemoteCallServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RemoteCallServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RemoteCallServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RemoteCallServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.ops.sc.rpc.grpc.callback.RpcCallBackRequest remoteCall(com.ops.sc.rpc.grpc.RemoteCallRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REMOTE_CALL, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class RemoteCallServiceFutureStub extends io.grpc.stub.AbstractStub<RemoteCallServiceFutureStub> {
    private RemoteCallServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RemoteCallServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RemoteCallServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RemoteCallServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.grpc.callback.RpcCallBackRequest> remoteCall(
        com.ops.sc.rpc.grpc.RemoteCallRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REMOTE_CALL, getCallOptions()), request);
    }
  }

  private static final int METHODID_REMOTE_CALL = 0;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final RemoteCallServiceImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(RemoteCallServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REMOTE_CALL:
          serviceImpl.remoteCall((com.ops.sc.rpc.grpc.RemoteCallRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.grpc.callback.RpcCallBackRequest>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    return new io.grpc.ServiceDescriptor(SERVICE_NAME,
        METHOD_REMOTE_CALL);
  }

}
