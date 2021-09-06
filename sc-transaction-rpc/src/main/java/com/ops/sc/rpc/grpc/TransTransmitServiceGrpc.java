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
    comments = "Source: msgTransmitService.proto")
public class TransTransmitServiceGrpc {

  private TransTransmitServiceGrpc() {}

  public static final String SERVICE_NAME = "TransTransmitService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.TransMessage,
      com.ops.sc.rpc.dto.TransResponse> METHOD_TRANSMIT_TRANS_MSG =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransTransmitService", "transmitTransMsg"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.TransMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.TransResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.StateServiceRequest,
      com.ops.sc.rpc.dto.StateServiceResponse> METHOD_STATE_CHECK =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransTransmitService", "stateCheck"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.StateServiceRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.StateServiceResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TransTransmitServiceStub newStub(io.grpc.Channel channel) {
    return new TransTransmitServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TransTransmitServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new TransTransmitServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static TransTransmitServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new TransTransmitServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class TransTransmitServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void transmitTransMsg(com.ops.sc.rpc.dto.TransMessage request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.TransResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_TRANSMIT_TRANS_MSG, responseObserver);
    }

    /**
     */
    public void stateCheck(com.ops.sc.rpc.dto.StateServiceRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.StateServiceResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_STATE_CHECK, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_TRANSMIT_TRANS_MSG,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.TransMessage,
                com.ops.sc.rpc.dto.TransResponse>(
                  this, METHODID_TRANSMIT_TRANS_MSG)))
          .addMethod(
            METHOD_STATE_CHECK,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.StateServiceRequest,
                com.ops.sc.rpc.dto.StateServiceResponse>(
                  this, METHODID_STATE_CHECK)))
          .build();
    }
  }

  /**
   */
  public static final class TransTransmitServiceStub extends io.grpc.stub.AbstractStub<TransTransmitServiceStub> {
    private TransTransmitServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private TransTransmitServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TransTransmitServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new TransTransmitServiceStub(channel, callOptions);
    }

    /**
     */
    public void transmitTransMsg(com.ops.sc.rpc.dto.TransMessage request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.TransResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_TRANSMIT_TRANS_MSG, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void stateCheck(com.ops.sc.rpc.dto.StateServiceRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.StateServiceResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_STATE_CHECK, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class TransTransmitServiceBlockingStub extends io.grpc.stub.AbstractStub<TransTransmitServiceBlockingStub> {
    private TransTransmitServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private TransTransmitServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TransTransmitServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new TransTransmitServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.ops.sc.rpc.dto.TransResponse transmitTransMsg(com.ops.sc.rpc.dto.TransMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_TRANSMIT_TRANS_MSG, getCallOptions(), request);
    }

    /**
     */
    public com.ops.sc.rpc.dto.StateServiceResponse stateCheck(com.ops.sc.rpc.dto.StateServiceRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_STATE_CHECK, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class TransTransmitServiceFutureStub extends io.grpc.stub.AbstractStub<TransTransmitServiceFutureStub> {
    private TransTransmitServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private TransTransmitServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TransTransmitServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new TransTransmitServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.TransResponse> transmitTransMsg(
        com.ops.sc.rpc.dto.TransMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_TRANSMIT_TRANS_MSG, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.StateServiceResponse> stateCheck(
        com.ops.sc.rpc.dto.StateServiceRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_STATE_CHECK, getCallOptions()), request);
    }
  }

  private static final int METHODID_TRANSMIT_TRANS_MSG = 0;
  private static final int METHODID_STATE_CHECK = 1;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final TransTransmitServiceImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(TransTransmitServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_TRANSMIT_TRANS_MSG:
          serviceImpl.transmitTransMsg((com.ops.sc.rpc.dto.TransMessage) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.TransResponse>) responseObserver);
          break;
        case METHODID_STATE_CHECK:
          serviceImpl.stateCheck((com.ops.sc.rpc.dto.StateServiceRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.StateServiceResponse>) responseObserver);
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
        METHOD_TRANSMIT_TRANS_MSG,
        METHOD_STATE_CHECK);
  }

}
