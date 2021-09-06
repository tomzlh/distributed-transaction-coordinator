package com.ops.sc.tc.state;


import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.tc.grpc.sync.SponsorGrpcSyncClient;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static com.ops.sc.common.constant.Constants.HEARTBEAT_INTERVAL;


public class SponsorHeartBeat {

    private static final Logger LOGGER = LoggerFactory.getLogger(SponsorHeartBeat.class);

    private static final ConcurrentHashMap<StreamObserver<RpcCallBackRequest>, HeartBeat> HEARTBEAT_MAP = new ConcurrentHashMap<>();

    private static final Long HEARTBEAT_TIMEOUT = 10 * 60 * 1000L;

    private SponsorHeartBeat() {
    }

    public static SponsorHeartBeat getInstance() {
        return HeartBeatServiceHolder.HEART_BEAT_SERVICE;
    }

    public void reset(StreamObserver<RpcCallBackRequest> streamObserverAdapter) {
        if (streamObserverAdapter == null) {
            return;
        }
        HeartBeat heartBeat = HEARTBEAT_MAP.get(streamObserverAdapter);

        if (heartBeat != null) {
            heartBeat.stop();
            HEARTBEAT_MAP.remove(streamObserverAdapter);
        }

    }

    public HeartBeat getHeartBeat(StreamObserver<RpcCallBackRequest> streamObserverAdapter) {
        if (streamObserverAdapter == null) {
            return null;
        }
        return HEARTBEAT_MAP.get(streamObserverAdapter);
    }

    public void start(StreamObserver<RpcCallBackRequest> asyncRequestObserver, String uniqueAppName, SponsorGrpcSyncClient sponsorGrpcSyncClient) {
        HeartBeat heartBeat = new HeartBeat(asyncRequestObserver, uniqueAppName, sponsorGrpcSyncClient);
        HEARTBEAT_MAP.putIfAbsent(asyncRequestObserver, heartBeat);
        SponsorGrpcSyncClient.tcExecutor.submit(heartBeat);
    }

    private static class HeartBeatServiceHolder {
        private static final SponsorHeartBeat HEART_BEAT_SERVICE = new SponsorHeartBeat();
    }

    public class HeartBeat implements Runnable {

        private volatile boolean isConnected = true;
        private StreamObserver<RpcCallBackRequest> observer;
        private String uniqueAppName;
        private Long lastHeartBeatTimeMills = System.currentTimeMillis();
        private SponsorGrpcSyncClient sponsorGrpcSyncClient;

        public HeartBeat(StreamObserver<RpcCallBackRequest> asyncRequestObserver, String uniqueAppName,
                SponsorGrpcSyncClient sponsorGrpcSyncClient) {
            this.observer = asyncRequestObserver;
            this.uniqueAppName = uniqueAppName;
            this.sponsorGrpcSyncClient = sponsorGrpcSyncClient;
        }

        @Override
        public void run() {
            for (;;) {
                if (!isConnected) {
                    break;
                }
                // server端心跳回包超时10min，客户端发起重连。
                if (System.currentTimeMillis() - lastHeartBeatTimeMills > HEARTBEAT_TIMEOUT) {
                    LOGGER.warn("{}: appName [{}] hasn't received heartBeat response more than {}s",
                            ClientErrorCode.HEART_BEAT_TIMEOUT.getErrorCode(), uniqueAppName, HEARTBEAT_TIMEOUT / 1000);
                    stop();
                    HEARTBEAT_MAP.remove(observer);
                    sponsorGrpcSyncClient.reconnect();
                    observer.onCompleted();
                    break;
                }
                try {
                    RpcCallBackRequest heartBeatRequest = RpcCallBackRequest.newBuilder()
                            .setAppName(uniqueAppName).setHeartBeatTag(Constants.HEARTBEAT_TAG).build();
                    LOGGER.debug("HeartBeat send to {} , appName: {}", sponsorGrpcSyncClient.getServerAddress(), uniqueAppName);
                    observer.onNext(heartBeatRequest);
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    LOGGER.error("HeartBeat interval may disorder", e);
                } catch (Throwable e) {
                    LOGGER.error("{}: send heartBeat to {} fail!", ClientErrorCode.INTERNAL_ERROR.getErrorCode(),
                            sponsorGrpcSyncClient.getServerAddress(), e);
                    break;
                }
            }
            LOGGER.info("Stop heart beat task. server address: {}, appName: {}", sponsorGrpcSyncClient.getServerAddress(),
                    uniqueAppName);
        }

        public void stop() {
            isConnected = false;
        }

        public void setLastHeartBeatTimeMills(Long lastHeartBeatTimeMills) {
            this.lastHeartBeatTimeMills = lastHeartBeatTimeMills;
        }
    }
}
