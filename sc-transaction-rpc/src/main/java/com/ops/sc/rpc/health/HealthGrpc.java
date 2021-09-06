package com.ops.sc.rpc.health;

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
    comments = "Source: healthCheck.proto")
public class HealthGrpc {

  private HealthGrpc() {}

  public static final String SERVICE_NAME = "com.ops.sc.rpc.health.Health";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest,
      com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse> METHOD_CHECK =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "com.ops.sc.rpc.health.Health", "Check"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest,
      com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse> METHOD_OBSERVE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
          generateFullMethodName(
              "com.ops.sc.rpc.health.Health", "Observe"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static HealthStub newStub(io.grpc.Channel channel) {
    return new HealthStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static HealthBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new HealthBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static HealthFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new HealthFutureStub(channel);
  }

  /**
   */
  public static abstract class HealthImplBase implements io.grpc.BindableService {

    /**
     */
    public void check(com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CHECK, responseObserver);
    }

    /**
     */
    public void observe(com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_OBSERVE, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_CHECK,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest,
                com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse>(
                  this, METHODID_CHECK)))
          .addMethod(
            METHOD_OBSERVE,
            asyncServerStreamingCall(
              new MethodHandlers<
                com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest,
                com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse>(
                  this, METHODID_OBSERVE)))
          .build();
    }
  }

  /**
   */
  public static final class HealthStub extends io.grpc.stub.AbstractStub<HealthStub> {
    private HealthStub(io.grpc.Channel channel) {
      super(channel);
    }

    private HealthStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HealthStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new HealthStub(channel, callOptions);
    }

    /**
     */
    public void check(com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CHECK, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void observe(com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_OBSERVE, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class HealthBlockingStub extends io.grpc.stub.AbstractStub<HealthBlockingStub> {
    private HealthBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private HealthBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HealthBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new HealthBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse check(com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CHECK, getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse> observe(
        com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_OBSERVE, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class HealthFutureStub extends io.grpc.stub.AbstractStub<HealthFutureStub> {
    private HealthFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private HealthFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HealthFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new HealthFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse> check(
        com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CHECK, getCallOptions()), request);
    }
  }

  private static final int METHODID_CHECK = 0;
  private static final int METHODID_OBSERVE = 1;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final HealthImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(HealthImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CHECK:
          serviceImpl.check((com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse>) responseObserver);
          break;
        case METHODID_OBSERVE:
          serviceImpl.observe((com.ops.sc.rpc.health.HealthCheck.HealthCheckRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.health.HealthCheck.HealthCheckResponse>) responseObserver);
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
        METHOD_CHECK,
        METHOD_OBSERVE);
  }

}
