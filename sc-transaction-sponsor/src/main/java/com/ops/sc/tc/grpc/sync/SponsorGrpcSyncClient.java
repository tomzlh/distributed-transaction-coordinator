package com.ops.sc.tc.grpc.sync;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.rpc.dto.*;
import com.ops.sc.rpc.grpc.TransactionManagerGrpc;
import com.ops.sc.rpc.grpc.callback.CallBackServiceGrpc;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;
import com.ops.sc.tc.grpc.GrpcClient;
import com.ops.sc.tc.grpc.GrpcInitParams;
import com.ops.sc.tc.grpc.async.StreamObserverProxy;
import com.ops.sc.tc.state.SponsorHeartBeat;
import io.grpc.Channel;
import io.grpc.Context;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.grpc.Status.Code.UNAVAILABLE;


public class SponsorGrpcSyncClient extends GrpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SponsorGrpcSyncClient.class);

    private static final int DEFAULT_SLEEP_MAX_MILLIS = 2 * 60 * 1000;
    private static final int DEFAULT_SLEEP_MILLIS = 100;
    private static final int MAX_RETRY_COUNT = 20;
    private int sleepTime = DEFAULT_SLEEP_MILLIS;
    private int retryCount = 0;

    private volatile StreamObserver<RpcCallBackRequest> requestObserverAdapter;
    private volatile StreamObserver<RpcCallBackRequest> asyncRequestObserver;

    public static ExecutorService tcExecutor;

    public SponsorGrpcSyncClient() {
        LOGGER.debug("Create new grpc client");
    }

    @Override
    public boolean shutdown() throws InterruptedException {
        tcExecutor.shutdown();
        return super.shutdown();
    }

    public void init(String serverAddress, GrpcInitParams config) {
        LOGGER.debug("Init grpc client, server address : {}, channel count : {}", serverAddress,
                config.getMaxChannelCount());
        this.serverAddress = serverAddress;
        super.createChannelFactory(serverAddress, config);
        tcExecutor = Executors.newFixedThreadPool(config.getThreadPoolSize());
        Context oldContext = Context.ROOT.attach();
        try {
            register();
        } finally {
            Context.ROOT.detach(oldContext);
        }

    }

    public GlobalTransResponse startGlobalTransSync(final GlobalTransRequest request, Long timeoutMS)
            throws RpcException {
        return syncCall(request, timeoutMS);
    }

    public GlobalSagaTransResponse startGlobalSagaTransSync(final GlobalSagaTransRequest request, Long timeoutMS)
            throws RpcException {
        return syncCall(request, timeoutMS);
    }


    public GlobalTransRollbackResponse rollbackGlobalTransSync(final GlobalTransRollbackRequest request, Long timeoutMS)
            throws RpcException {
        return syncCall(request, timeoutMS);
    }

    public BranchTransResponse commitBranchTransSync(final BranchTransRequest request,Long timeoutMS) throws RpcException{
        return syncCall(request, timeoutMS);
    }

    public BranchTransResponse rollbackBranchTransSync(final BranchTransRequest request,Long timeoutMS) throws RpcException{
        return syncCall(request, timeoutMS);
    }

    public TransQueryResponse findGlobalTransSync(final TransQueryRequest transQueryRequest,Long timeoutMS)
            throws RpcException {
        return syncCall(transQueryRequest, timeoutMS);
    }


    public MQProducerRegResponse producerRegisterSync(final MQProducerRegRequest request, Long timeoutMills)
            throws RpcException {
        return syncCall(request, timeoutMills);
    }

    public StateServiceResponse stateCheckSync(final StateServiceRequest request, Long timeoutMills) throws RpcException {
        return syncCall(request, timeoutMills);
    }



    private <T> T syncCall(Object request, Long timeoutMills) throws RpcException {
        try {
            Channel channel = channelFactory.getChannel();
            TransactionManagerGrpc.TransactionManagerBlockingStub stub = TransactionManagerGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(timeoutMills, TimeUnit.MILLISECONDS);
            if (request instanceof GlobalTransRequest) {
                return (T) stub.startGlobalTrans((GlobalTransRequest) request);
            }else if(request instanceof GlobalSagaTransRequest){
                return (T) stub.startSagaGlobalTrans((GlobalSagaTransRequest) request);
            }else if (request instanceof GlobalTransRollbackRequest) {
                return (T) stub.rollbackGlobalTrans((GlobalTransRollbackRequest) request);
            }else if (request instanceof MQProducerRegRequest) {
                return (T) stub.registerProducer((MQProducerRegRequest) request);
            }else if (request instanceof StateServiceRequest) {
                return (T) stub.stateCheck((StateServiceRequest) request);
            }else if(request instanceof TransQueryRequest){
                return (T) stub.findGlobalTrans((TransQueryRequest) request);
            }else if(request instanceof BranchTransRequest){
                return (T) stub.executeBranchTrans((BranchTransRequest) request);
            }
            else {
                throw new IllegalArgumentException("Not support this request type");
            }
        } catch (StatusRuntimeException e) {
            LOGGER.error("Request to server {} error: {}", serverAddress, e.getMessage());
            throw new RpcException(e.getStatus().asException());
        }
    }


    public void reconnect() {
        register();
    }

    private void register() {
        sleepSometime();
        retryCount++;
        if (retryCount > MAX_RETRY_COUNT) {
            // 超过最大重试次数，暂停连接
            LOGGER.warn("RMClient connect to server: {} failed {} times, close ts client", serverAddress,
                    retryCount);
            SponsorGrpcSyncClientBoot.getInstance().closeTSClient(serverAddress);
            return;
        }
        Channel channel = channelFactory.getChannel();
        // 请求超时
        CallBackServiceGrpc.CallBackServiceStub asyncStub = CallBackServiceGrpc.newStub(channel);
        LOGGER.debug("Start to register rmClient : {}", serverAddress);
        asyncRequestObserver = asyncStub.call(new StreamObserver<RpcCallBackResponse>() {
            @Override
            public void onNext(RpcCallBackResponse rpcCallBackResponse) {
                try {
                    if (rpcCallBackResponse.getRegisterResult()) { // 注册成功后发起心跳
                        LOGGER.info("{} has connected to rpcServer {} success!", rpcCallBackResponse.getAppName(),
                                serverAddress);
                        sleepTime = DEFAULT_SLEEP_MILLIS;
                        retryCount = 0;
                        appName = rpcCallBackResponse.getAppName();
                        SponsorHeartBeat.getInstance().start(requestObserverAdapter,
                                rpcCallBackResponse.getAppName(),
                                SponsorGrpcSyncClientBoot.getInstance().getTSClient(serverAddress));
                    } else if (rpcCallBackResponse.getHeartBeatResult()) {
                        LOGGER.debug("HeartBeat return from: {} , appName: {}", serverAddress,
                                rpcCallBackResponse.getAppName());
                        SponsorHeartBeat.HeartBeat heartBeat = SponsorHeartBeat.getInstance()
                                .getHeartBeat(requestObserverAdapter);
                        if (heartBeat != null) {
                            heartBeat.setLastHeartBeatTimeMills(System.currentTimeMillis());
                        }
                    } else {
                        LOGGER.info("not supported rpc response message type! {}",rpcCallBackResponse);
                    }
                } catch (Throwable e) {
                    LOGGER.error("CallBack Error!", e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                // 客户端状态重置
                reset();
                if (throwable instanceof StatusRuntimeException
                        && UNAVAILABLE == ((StatusRuntimeException) throwable).getStatus().getCode()) {
                    LOGGER.error("{}: RpcServer {} unavailable!", ClientErrorCode.SERVER_NOT_AVAILABLE.getErrorCode(),
                            serverAddress);
                } else {
                    LOGGER.error("{}: RpcServer {} disconnect actively! error message : {} ",
                            ClientErrorCode.SERVER_NOT_AVAILABLE.getErrorCode(), serverAddress, throwable.getMessage());
                }
                // 客户端发起重连
                register();
            }
            @Override
            public void onCompleted() {
                LOGGER.info("Sponsor Client {} onComplete!", serverAddress);
            }
        });
        requestObserverAdapter = new StreamObserverProxy(asyncRequestObserver);
        tcExecutor.submit(new Runnable() {
            @Override
            public void run() {
                RpcCallBackRequest rpcCallBackRequest = RpcCallBackRequest.newBuilder().setAppName(appName)
                        .setHeartBeatTag(Constants.REGISTER).build();
                requestObserverAdapter.onNext(rpcCallBackRequest);
            }
        });
    }

    private void reset() {
        asyncRequestObserver = null;
        SponsorHeartBeat.getInstance().reset(requestObserverAdapter);
        requestObserverAdapter = null;
    }


    private void sleepSometime() {
        int floatTime = new Random().nextInt(sleepTime / 5);
        int sleepTime = this.sleepTime + floatTime;
        sleepTime = sleepTime > DEFAULT_SLEEP_MAX_MILLIS ? DEFAULT_SLEEP_MAX_MILLIS : sleepTime;
        this.sleepTime = 2 * sleepTime;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOGGER.warn("Register sleep interrupted.", e);
        }
    }



    public String getServerAddress() {
        return serverAddress;
    }



}
