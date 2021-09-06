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
    comments = "Source: transLifeManager.proto")
public class TransactionManagerGrpc {

  private TransactionManagerGrpc() {}

  public static final String SERVICE_NAME = "TransactionManager";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.BranchTransRequest,
      com.ops.sc.rpc.dto.BranchTransResponse> METHOD_START_BRANCH_TRANS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransactionManager", "startBranchTrans"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.BranchTransRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.BranchTransResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.GlobalTransRequest,
      com.ops.sc.rpc.dto.GlobalTransResponse> METHOD_START_GLOBAL_TRANS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransactionManager", "startGlobalTrans"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.GlobalTransRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.GlobalTransResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.GlobalSagaTransRequest,
      com.ops.sc.rpc.dto.GlobalSagaTransResponse> METHOD_START_SAGA_GLOBAL_TRANS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransactionManager", "startSagaGlobalTrans"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.GlobalSagaTransRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.GlobalSagaTransResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.GlobalTransRollbackRequest,
      com.ops.sc.rpc.dto.GlobalTransRollbackResponse> METHOD_ROLLBACK_GLOBAL_TRANS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransactionManager", "rollbackGlobalTrans"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.GlobalTransRollbackRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.GlobalTransRollbackResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.RegTransMsgRequest,
      com.ops.sc.rpc.dto.RegTransMsgResponse> METHOD_REG_TRANS_MSG =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransactionManager", "regTransMsg"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.RegTransMsgRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.RegTransMsgResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.MQProducerRegRequest,
      com.ops.sc.rpc.dto.MQProducerRegResponse> METHOD_REGISTER_PRODUCER =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransactionManager", "registerProducer"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.MQProducerRegRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.MQProducerRegResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.StateServiceRequest,
      com.ops.sc.rpc.dto.StateServiceResponse> METHOD_STATE_CHECK =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransactionManager", "stateCheck"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.StateServiceRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.StateServiceResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.TransQueryRequest,
      com.ops.sc.rpc.dto.TransQueryResponse> METHOD_FIND_GLOBAL_TRANS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransactionManager", "findGlobalTrans"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.TransQueryRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.TransQueryResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.BranchTransRequest,
      com.ops.sc.rpc.dto.BranchTransResponse> METHOD_EXECUTE_BRANCH_TRANS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransactionManager", "executeBranchTrans"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.BranchTransRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.BranchTransResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.ops.sc.rpc.dto.TransCompensationRequest,
      com.ops.sc.rpc.dto.TransCompensationResponse> METHOD_COMPENSATE_TRANS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "TransactionManager", "compensateTrans"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.TransCompensationRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.ops.sc.rpc.dto.TransCompensationResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TransactionManagerStub newStub(io.grpc.Channel channel) {
    return new TransactionManagerStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TransactionManagerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new TransactionManagerBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static TransactionManagerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new TransactionManagerFutureStub(channel);
  }

  /**
   */
  public static abstract class TransactionManagerImplBase implements io.grpc.BindableService {

    /**
     */
    public void startBranchTrans(com.ops.sc.rpc.dto.BranchTransRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.BranchTransResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_START_BRANCH_TRANS, responseObserver);
    }

    /**
     */
    public void startGlobalTrans(com.ops.sc.rpc.dto.GlobalTransRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.GlobalTransResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_START_GLOBAL_TRANS, responseObserver);
    }

    /**
     */
    public void startSagaGlobalTrans(com.ops.sc.rpc.dto.GlobalSagaTransRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.GlobalSagaTransResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_START_SAGA_GLOBAL_TRANS, responseObserver);
    }

    /**
     */
    public void rollbackGlobalTrans(com.ops.sc.rpc.dto.GlobalTransRollbackRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.GlobalTransRollbackResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ROLLBACK_GLOBAL_TRANS, responseObserver);
    }

    /**
     */
    public void regTransMsg(com.ops.sc.rpc.dto.RegTransMsgRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.RegTransMsgResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_REG_TRANS_MSG, responseObserver);
    }

    /**
     */
    public void registerProducer(com.ops.sc.rpc.dto.MQProducerRegRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.MQProducerRegResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_REGISTER_PRODUCER, responseObserver);
    }

    /**
     */
    public void stateCheck(com.ops.sc.rpc.dto.StateServiceRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.StateServiceResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_STATE_CHECK, responseObserver);
    }

    /**
     */
    public void findGlobalTrans(com.ops.sc.rpc.dto.TransQueryRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.TransQueryResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_FIND_GLOBAL_TRANS, responseObserver);
    }

    /**
     */
    public void executeBranchTrans(com.ops.sc.rpc.dto.BranchTransRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.BranchTransResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_EXECUTE_BRANCH_TRANS, responseObserver);
    }

    /**
     */
    public void compensateTrans(com.ops.sc.rpc.dto.TransCompensationRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.TransCompensationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_COMPENSATE_TRANS, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_START_BRANCH_TRANS,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.BranchTransRequest,
                com.ops.sc.rpc.dto.BranchTransResponse>(
                  this, METHODID_START_BRANCH_TRANS)))
          .addMethod(
            METHOD_START_GLOBAL_TRANS,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.GlobalTransRequest,
                com.ops.sc.rpc.dto.GlobalTransResponse>(
                  this, METHODID_START_GLOBAL_TRANS)))
          .addMethod(
            METHOD_START_SAGA_GLOBAL_TRANS,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.GlobalSagaTransRequest,
                com.ops.sc.rpc.dto.GlobalSagaTransResponse>(
                  this, METHODID_START_SAGA_GLOBAL_TRANS)))
          .addMethod(
            METHOD_ROLLBACK_GLOBAL_TRANS,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.GlobalTransRollbackRequest,
                com.ops.sc.rpc.dto.GlobalTransRollbackResponse>(
                  this, METHODID_ROLLBACK_GLOBAL_TRANS)))
          .addMethod(
            METHOD_REG_TRANS_MSG,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.RegTransMsgRequest,
                com.ops.sc.rpc.dto.RegTransMsgResponse>(
                  this, METHODID_REG_TRANS_MSG)))
          .addMethod(
            METHOD_REGISTER_PRODUCER,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.MQProducerRegRequest,
                com.ops.sc.rpc.dto.MQProducerRegResponse>(
                  this, METHODID_REGISTER_PRODUCER)))
          .addMethod(
            METHOD_STATE_CHECK,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.StateServiceRequest,
                com.ops.sc.rpc.dto.StateServiceResponse>(
                  this, METHODID_STATE_CHECK)))
          .addMethod(
            METHOD_FIND_GLOBAL_TRANS,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.TransQueryRequest,
                com.ops.sc.rpc.dto.TransQueryResponse>(
                  this, METHODID_FIND_GLOBAL_TRANS)))
          .addMethod(
            METHOD_EXECUTE_BRANCH_TRANS,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.BranchTransRequest,
                com.ops.sc.rpc.dto.BranchTransResponse>(
                  this, METHODID_EXECUTE_BRANCH_TRANS)))
          .addMethod(
            METHOD_COMPENSATE_TRANS,
            asyncUnaryCall(
              new MethodHandlers<
                com.ops.sc.rpc.dto.TransCompensationRequest,
                com.ops.sc.rpc.dto.TransCompensationResponse>(
                  this, METHODID_COMPENSATE_TRANS)))
          .build();
    }
  }

  /**
   */
  public static final class TransactionManagerStub extends io.grpc.stub.AbstractStub<TransactionManagerStub> {
    private TransactionManagerStub(io.grpc.Channel channel) {
      super(channel);
    }

    private TransactionManagerStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TransactionManagerStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new TransactionManagerStub(channel, callOptions);
    }

    /**
     */
    public void startBranchTrans(com.ops.sc.rpc.dto.BranchTransRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.BranchTransResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_START_BRANCH_TRANS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void startGlobalTrans(com.ops.sc.rpc.dto.GlobalTransRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.GlobalTransResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_START_GLOBAL_TRANS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void startSagaGlobalTrans(com.ops.sc.rpc.dto.GlobalSagaTransRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.GlobalSagaTransResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_START_SAGA_GLOBAL_TRANS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void rollbackGlobalTrans(com.ops.sc.rpc.dto.GlobalTransRollbackRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.GlobalTransRollbackResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ROLLBACK_GLOBAL_TRANS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void regTransMsg(com.ops.sc.rpc.dto.RegTransMsgRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.RegTransMsgResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REG_TRANS_MSG, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void registerProducer(com.ops.sc.rpc.dto.MQProducerRegRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.MQProducerRegResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REGISTER_PRODUCER, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void stateCheck(com.ops.sc.rpc.dto.StateServiceRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.StateServiceResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_STATE_CHECK, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findGlobalTrans(com.ops.sc.rpc.dto.TransQueryRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.TransQueryResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_FIND_GLOBAL_TRANS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void executeBranchTrans(com.ops.sc.rpc.dto.BranchTransRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.BranchTransResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_BRANCH_TRANS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void compensateTrans(com.ops.sc.rpc.dto.TransCompensationRequest request,
        io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.TransCompensationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_COMPENSATE_TRANS, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class TransactionManagerBlockingStub extends io.grpc.stub.AbstractStub<TransactionManagerBlockingStub> {
    private TransactionManagerBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private TransactionManagerBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TransactionManagerBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new TransactionManagerBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.ops.sc.rpc.dto.BranchTransResponse startBranchTrans(com.ops.sc.rpc.dto.BranchTransRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_START_BRANCH_TRANS, getCallOptions(), request);
    }

    /**
     */
    public com.ops.sc.rpc.dto.GlobalTransResponse startGlobalTrans(com.ops.sc.rpc.dto.GlobalTransRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_START_GLOBAL_TRANS, getCallOptions(), request);
    }

    /**
     */
    public com.ops.sc.rpc.dto.GlobalSagaTransResponse startSagaGlobalTrans(com.ops.sc.rpc.dto.GlobalSagaTransRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_START_SAGA_GLOBAL_TRANS, getCallOptions(), request);
    }

    /**
     */
    public com.ops.sc.rpc.dto.GlobalTransRollbackResponse rollbackGlobalTrans(com.ops.sc.rpc.dto.GlobalTransRollbackRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ROLLBACK_GLOBAL_TRANS, getCallOptions(), request);
    }

    /**
     */
    public com.ops.sc.rpc.dto.RegTransMsgResponse regTransMsg(com.ops.sc.rpc.dto.RegTransMsgRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REG_TRANS_MSG, getCallOptions(), request);
    }

    /**
     */
    public com.ops.sc.rpc.dto.MQProducerRegResponse registerProducer(com.ops.sc.rpc.dto.MQProducerRegRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REGISTER_PRODUCER, getCallOptions(), request);
    }

    /**
     */
    public com.ops.sc.rpc.dto.StateServiceResponse stateCheck(com.ops.sc.rpc.dto.StateServiceRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_STATE_CHECK, getCallOptions(), request);
    }

    /**
     */
    public com.ops.sc.rpc.dto.TransQueryResponse findGlobalTrans(com.ops.sc.rpc.dto.TransQueryRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_FIND_GLOBAL_TRANS, getCallOptions(), request);
    }

    /**
     */
    public com.ops.sc.rpc.dto.BranchTransResponse executeBranchTrans(com.ops.sc.rpc.dto.BranchTransRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXECUTE_BRANCH_TRANS, getCallOptions(), request);
    }

    /**
     */
    public com.ops.sc.rpc.dto.TransCompensationResponse compensateTrans(com.ops.sc.rpc.dto.TransCompensationRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_COMPENSATE_TRANS, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class TransactionManagerFutureStub extends io.grpc.stub.AbstractStub<TransactionManagerFutureStub> {
    private TransactionManagerFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private TransactionManagerFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TransactionManagerFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new TransactionManagerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.BranchTransResponse> startBranchTrans(
        com.ops.sc.rpc.dto.BranchTransRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_START_BRANCH_TRANS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.GlobalTransResponse> startGlobalTrans(
        com.ops.sc.rpc.dto.GlobalTransRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_START_GLOBAL_TRANS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.GlobalSagaTransResponse> startSagaGlobalTrans(
        com.ops.sc.rpc.dto.GlobalSagaTransRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_START_SAGA_GLOBAL_TRANS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.GlobalTransRollbackResponse> rollbackGlobalTrans(
        com.ops.sc.rpc.dto.GlobalTransRollbackRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ROLLBACK_GLOBAL_TRANS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.RegTransMsgResponse> regTransMsg(
        com.ops.sc.rpc.dto.RegTransMsgRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REG_TRANS_MSG, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.MQProducerRegResponse> registerProducer(
        com.ops.sc.rpc.dto.MQProducerRegRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REGISTER_PRODUCER, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.StateServiceResponse> stateCheck(
        com.ops.sc.rpc.dto.StateServiceRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_STATE_CHECK, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.TransQueryResponse> findGlobalTrans(
        com.ops.sc.rpc.dto.TransQueryRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_FIND_GLOBAL_TRANS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.BranchTransResponse> executeBranchTrans(
        com.ops.sc.rpc.dto.BranchTransRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_BRANCH_TRANS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ops.sc.rpc.dto.TransCompensationResponse> compensateTrans(
        com.ops.sc.rpc.dto.TransCompensationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_COMPENSATE_TRANS, getCallOptions()), request);
    }
  }

  private static final int METHODID_START_BRANCH_TRANS = 0;
  private static final int METHODID_START_GLOBAL_TRANS = 1;
  private static final int METHODID_START_SAGA_GLOBAL_TRANS = 2;
  private static final int METHODID_ROLLBACK_GLOBAL_TRANS = 3;
  private static final int METHODID_REG_TRANS_MSG = 4;
  private static final int METHODID_REGISTER_PRODUCER = 5;
  private static final int METHODID_STATE_CHECK = 6;
  private static final int METHODID_FIND_GLOBAL_TRANS = 7;
  private static final int METHODID_EXECUTE_BRANCH_TRANS = 8;
  private static final int METHODID_COMPENSATE_TRANS = 9;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final TransactionManagerImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(TransactionManagerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_START_BRANCH_TRANS:
          serviceImpl.startBranchTrans((com.ops.sc.rpc.dto.BranchTransRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.BranchTransResponse>) responseObserver);
          break;
        case METHODID_START_GLOBAL_TRANS:
          serviceImpl.startGlobalTrans((com.ops.sc.rpc.dto.GlobalTransRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.GlobalTransResponse>) responseObserver);
          break;
        case METHODID_START_SAGA_GLOBAL_TRANS:
          serviceImpl.startSagaGlobalTrans((com.ops.sc.rpc.dto.GlobalSagaTransRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.GlobalSagaTransResponse>) responseObserver);
          break;
        case METHODID_ROLLBACK_GLOBAL_TRANS:
          serviceImpl.rollbackGlobalTrans((com.ops.sc.rpc.dto.GlobalTransRollbackRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.GlobalTransRollbackResponse>) responseObserver);
          break;
        case METHODID_REG_TRANS_MSG:
          serviceImpl.regTransMsg((com.ops.sc.rpc.dto.RegTransMsgRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.RegTransMsgResponse>) responseObserver);
          break;
        case METHODID_REGISTER_PRODUCER:
          serviceImpl.registerProducer((com.ops.sc.rpc.dto.MQProducerRegRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.MQProducerRegResponse>) responseObserver);
          break;
        case METHODID_STATE_CHECK:
          serviceImpl.stateCheck((com.ops.sc.rpc.dto.StateServiceRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.StateServiceResponse>) responseObserver);
          break;
        case METHODID_FIND_GLOBAL_TRANS:
          serviceImpl.findGlobalTrans((com.ops.sc.rpc.dto.TransQueryRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.TransQueryResponse>) responseObserver);
          break;
        case METHODID_EXECUTE_BRANCH_TRANS:
          serviceImpl.executeBranchTrans((com.ops.sc.rpc.dto.BranchTransRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.BranchTransResponse>) responseObserver);
          break;
        case METHODID_COMPENSATE_TRANS:
          serviceImpl.compensateTrans((com.ops.sc.rpc.dto.TransCompensationRequest) request,
              (io.grpc.stub.StreamObserver<com.ops.sc.rpc.dto.TransCompensationResponse>) responseObserver);
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
        METHOD_START_BRANCH_TRANS,
        METHOD_START_GLOBAL_TRANS,
        METHOD_START_SAGA_GLOBAL_TRANS,
        METHOD_ROLLBACK_GLOBAL_TRANS,
        METHOD_REG_TRANS_MSG,
        METHOD_REGISTER_PRODUCER,
        METHOD_STATE_CHECK,
        METHOD_FIND_GLOBAL_TRANS,
        METHOD_EXECUTE_BRANCH_TRANS,
        METHOD_COMPENSATE_TRANS);
  }

}
