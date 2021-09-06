package com.ops.sc.rpc.grpc.callback;

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
    comments = "Source: callbackService.proto")
public class CallBackServiceGrpc {

  private CallBackServiceGrpc() {}

  public static final String SERVICE_NAME = "CallBackService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.grpc.callback.RpcCallBackRequest,
      com.ops.sc.rpc.grpc.callback.RpcCallBackResponse> METHOD_CALL =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING,
          generateFullMethodName(
              "CallBackService", "call"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.grpc.callback.RpcCallBackRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.grpc.callback.RpcCallBackResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CallBackServiceStub newStub(io.grpc.Channel channel) {
    return new CallBackServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CallBackServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CallBackServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static CallBackServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CallBackServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class CallBackServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<com.ops.sc.rpc.grpc.callback.RpcCallBackRequest> call(
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.grpc.callback.RpcCallBackResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_CALL, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_CALL,
            asyncBidiStreamingCall(
              new MethodHandlers<
                com.ops.sc.rpc.grpc.callback.RpcCallBackRequest,
                com.ops.sc.rpc.grpc.callback.RpcCallBackResponse>(
                  this, METHODID_CALL)))
          .build();
    }
  }

  /**
   */
  public static final class CallBackServiceStub extends io.grpc.stub.AbstractStub<CallBackServiceStub> {
    private CallBackServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CallBackServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CallBackServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CallBackServiceStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.ops.sc.rpc.grpc.callback.RpcCallBackRequest> call(
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.grpc.callback.RpcCallBackResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_CALL, getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class CallBackServiceBlockingStub extends io.grpc.stub.AbstractStub<CallBackServiceBlockingStub> {
    private CallBackServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CallBackServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CallBackServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CallBackServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   */
  public static final class CallBackServiceFutureStub extends io.grpc.stub.AbstractStub<CallBackServiceFutureStub> {
    private CallBackServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CallBackServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CallBackServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CallBackServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_CALL = 0;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CallBackServiceImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(CallBackServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CALL:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.call(
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.grpc.callback.RpcCallBackResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    return new io.grpc.ServiceDescriptor(SERVICE_NAME,
        METHOD_CALL);
  }

}
