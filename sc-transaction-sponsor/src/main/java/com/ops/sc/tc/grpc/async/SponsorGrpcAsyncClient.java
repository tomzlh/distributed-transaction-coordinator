package com.ops.sc.tc.grpc.async;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.utils.StringTools;
import com.ops.sc.rpc.grpc.callback.CallBackServiceGrpc;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;
import com.ops.sc.tc.executor.ScCallBackExecutor;
import com.ops.sc.tc.grpc.GrpcClient;
import com.ops.sc.tc.grpc.GrpcInitParams;
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

import static io.grpc.Status.Code.UNAVAILABLE;


public class SponsorGrpcAsyncClient extends GrpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SponsorGrpcAsyncClient.class);
    private static final int DEFAULT_SLEEP_MAX_MILLIS = 2 * 60 * 1000;
    private static final int DEFAULT_SLEEP_INIT_MILLIS = 100;
    private static final int MAX_RETRY_COUNT = 20;
    public static ExecutorService rmExecutor;
    private volatile StreamObserver<RpcCallBackRequest> requestObserverAdapter;
    private volatile StreamObserver<RpcCallBackRequest> asyncRequestObserver;
    private int sleepTime = DEFAULT_SLEEP_INIT_MILLIS;
    private int retryCount = 0;
    private volatile String uniqueAppName;

    public SponsorGrpcAsyncClient() {
        LOGGER.debug("Create a new sponsor client");
    }

    public void init(String serverAddress, GrpcInitParams config) {
        LOGGER.debug("Init sponsor client, server address : {}, channel count : {}", serverAddress,
                config.getMaxChannelCount());
        this.serverAddress = serverAddress;
        super.createChannelFactory(serverAddress, config);
        rmExecutor = Executors.newFixedThreadPool(config.getThreadPoolSize());

        Context oldContext = Context.ROOT.attach();
        try {
            register();
        } finally {
            Context.ROOT.detach(oldContext);
        }
    }

    @Override
    public boolean shutdown() throws InterruptedException {
        // ???????????????STOP,????????????,????????????
        rmExecutor.shutdownNow();
        return super.shutdown();
    }


    public void reconnect() {
        register();
    }

    private void register() {
        sleepSometime();
        retryCount++;
        if (retryCount > MAX_RETRY_COUNT) {
            // ???????????????????????????????????????
            LOGGER.warn("TSClient connect to server: {} fail for {} times, close tsClient", serverAddress,
                    retryCount);
            SponsorGrpcAsyncClientBoot.getInstance().closeTSClient(serverAddress);
            return;
        }
        Channel channel = channelFactory.getChannel();
        // ????????????
        CallBackServiceGrpc.CallBackServiceStub asyncStub = CallBackServiceGrpc.newStub(channel);
        LOGGER.debug("Start to register tsClient : {}", serverAddress);
        asyncRequestObserver = asyncStub.call(new StreamObserver<RpcCallBackResponse>() {
            @Override
            public void onNext(RpcCallBackResponse rpcCallBackResponse) {
                try {
                    if (rpcCallBackResponse.getRegisterResult()) { // ???????????????????????????
                        LOGGER.info("{} has connected to rpcServer {} success!", rpcCallBackResponse.getAppName(),
                                serverAddress);
                        // ?????????????????????
                        sleepTime = DEFAULT_SLEEP_INIT_MILLIS;
                        retryCount = 0;
                        uniqueAppName = rpcCallBackResponse.getAppName();
                        /*SponsorHeartBeat.getInstance().start(requestObserverAdapter,
                                rpcCallBackResponse.getAppName(),
                                SponsorGrpcAsyncClientBoot.getInstance().getRMClient(serverAddress));*/
                    } else if (rpcCallBackResponse.getHeartBeatResult()) { // rpcServer????????????,??????????????????????????????
                        LOGGER.debug("HeartBeat return from: {} , appName: {}", serverAddress,
                                rpcCallBackResponse.getAppName());
                        SponsorHeartBeat.HeartBeat heartBeat = SponsorHeartBeat.getInstance()
                                .getHeartBeat(requestObserverAdapter);
                        if (heartBeat != null) {
                            heartBeat.setLastHeartBeatTimeMills(System.currentTimeMillis());
                        }
                    } else { // ??????????????????
                        rmExecutor.submit(new Runnable() {
                            @Override
                            public void run() {
                                ScCallBackExecutor.getInstance().handleCallBack(rpcCallBackResponse, requestObserverAdapter);
                            }
                        });
                    }
                } catch (Throwable e) {
                    LOGGER.error("CallBack Error!", e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                // ?????????????????????
                reset();
                if (throwable instanceof StatusRuntimeException
                        && UNAVAILABLE == ((StatusRuntimeException) throwable).getStatus().getCode()) {
                    LOGGER.error("{}: RpcServer {} unavailable!", ClientErrorCode.SERVER_NOT_AVAILABLE.getErrorCode(),
                            serverAddress);
                } else {
                    LOGGER.error("{}: RpcServer {} disconnect actively! error message : {} ",
                            ClientErrorCode.SERVER_NOT_AVAILABLE.getErrorCode(), serverAddress, throwable.getMessage());
                }
                // ?????????????????????
                register();
            }
            @Override
            public void onCompleted() {
                LOGGER.info("Sponsor Client {} onComplete! asyncRequestObserver: {}", serverAddress, asyncRequestObserver);
            }
        });
        requestObserverAdapter = new StreamObserverProxy(asyncRequestObserver);
        rmExecutor.submit(new Runnable() {
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
        int time = sleepTime + floatTime;
        sleepTime = sleepTime > DEFAULT_SLEEP_MAX_MILLIS ? DEFAULT_SLEEP_MAX_MILLIS : sleepTime;
        sleepTime = 2 * sleepTime;
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            LOGGER.warn("Register back off interrupted.", e);
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }


}