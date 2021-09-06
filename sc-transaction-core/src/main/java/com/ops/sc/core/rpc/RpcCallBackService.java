package com.ops.sc.core.rpc;


import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.constant.RpcConstants;
import com.ops.sc.common.utils.InetUtil;
import com.ops.sc.common.utils.StringTools;
import com.ops.sc.common.reg.zk.ZookeeperRegistryCenter;
import com.ops.sc.rpc.grpc.callback.CallBackServiceGrpc;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Service
public class RpcCallBackService extends CallBackServiceGrpc.CallBackServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConstants.SC_LOG);
    private static final Map<String, ConcurrentHashMap<String, StreamObserver>> STREAM_OBSERVER_MAP = Maps
            .newConcurrentMap();
    private static final Map<String, CountDownLatch> REQUEST_LATCH_MAP = Maps.newConcurrentMap();

    private static final Map<String, StreamObserver<RpcCallBackRequest>> REMOTE_SERVER_OBSERVER_MAP = Maps
            .newConcurrentMap();
    private static final Map<String, RpcCallBackRequest> REQUEST_MAP = Maps.newConcurrentMap();


    /**
     * 从本地map中随机获取一个符合要求的observer
     *
     * @param appName
     * @return
     */
    public static StreamObserver<RpcCallBackResponse> getLocalAvailableStreamObserver(String appName) {
        return getLocalAvailableStreamObserver(appName, null);
    }

    /**
     * 从本地map中获取observer，若id为null，则随机获取一个否则获取指定id的observer
     *
     * @param appName
     * @param uniqueId
     * @return
     */
    public static StreamObserver<RpcCallBackResponse> getLocalAvailableStreamObserver(String appName, String uniqueId) {
        Map<String, StreamObserver> streamObserverMap = STREAM_OBSERVER_MAP.get(appName);

        if (streamObserverMap == null || streamObserverMap.isEmpty()) {
            LOGGER.warn("No available local cached streamObserver for appName: {} at {}", appName,
                    InetUtil.getHostIp());
            return null;
        }
        Preconditions.checkNotNull(streamObserverMap.values(), "No available local cached streamObserver");
        if (StringUtils.isNotBlank(uniqueId)) {
            return streamObserverMap.get(uniqueId);
        } else {
            Object[] streamObservers = streamObserverMap.values().toArray();
            return (StreamObserver) streamObservers[new Random().nextInt(streamObservers.length)];
        }
    }

    private static void addLocalAvailableStreamObserver(String appNameUnique, StreamObserver streamObserver) {
        String appName = StringTools.getAppNameOnly(appNameUnique);
        String uniqueId = StringTools.getUniqueIdOnly(appNameUnique);
        ConcurrentHashMap<String, StreamObserver> observerMap = STREAM_OBSERVER_MAP.get(appName);
        synchronized (STREAM_OBSERVER_MAP) {
            if (observerMap == null) {
                ConcurrentHashMap<String, StreamObserver> newObserverMap = new ConcurrentHashMap<>();
                newObserverMap.put(uniqueId, streamObserver);
                STREAM_OBSERVER_MAP.put(appName, newObserverMap);
            } else {
                observerMap.put(uniqueId, streamObserver);
                STREAM_OBSERVER_MAP.put(appName, observerMap);
            }
        }
    }

    public static void setObserverLatchMap(String requestId, CountDownLatch countDownLatch) {
        REQUEST_LATCH_MAP.putIfAbsent(requestId, countDownLatch);
    }

    public static void setRemoteServerObserverMap(String requestId, StreamObserver<RpcCallBackRequest> observer) {
        REMOTE_SERVER_OBSERVER_MAP.putIfAbsent(requestId, observer);
    }

    public static RpcCallBackRequest getRpcCallBackRequest(String requestId) {
        return REQUEST_MAP.get(requestId);
    }

    private static void refreshLocalAvailableObserver(StreamObserver<RpcCallBackResponse> responseObserver) {
        for (Iterator<ConcurrentHashMap.Entry<String, ConcurrentHashMap<String, StreamObserver>>> mapIterator = STREAM_OBSERVER_MAP
                .entrySet().iterator(); mapIterator.hasNext();) {
            boolean loop = true;
            ConcurrentHashMap.Entry<String, ConcurrentHashMap<String, StreamObserver>> iterator = mapIterator.next();
            for (Iterator<ConcurrentHashMap.Entry<String, StreamObserver>> subIterator = iterator.getValue().entrySet()
                    .iterator(); subIterator.hasNext();) {
                ConcurrentHashMap.Entry<String, StreamObserver> map = subIterator.next();
                if (map.getValue() == responseObserver) {
                    LOGGER.warn("RpcClient appName: {}, UUID: {} disconnect.", iterator.getKey(), map.getKey());
                    subIterator.remove();
                    loop = false;
                    if (STREAM_OBSERVER_MAP.get(iterator.getKey()).size() == 0) {
                        mapIterator.remove();
                    }
                    break;
                }
            }
            if (!loop) {
                break;
            }
        }
    }

    public static void removeLocalObserverByAppName(String appNameUnique) {
        String appName = StringTools.getAppNameOnly(appNameUnique);
        String uniqueId = StringTools.getUniqueIdOnly(appNameUnique);
        Preconditions.checkNotNull(appName, "appName cannot be null");
        Preconditions.checkNotNull(uniqueId, "uniqueId cannot be null");
        ConcurrentHashMap<String, StreamObserver> uniqueObserverMap = STREAM_OBSERVER_MAP.get(appName);

        if (uniqueObserverMap == null || uniqueObserverMap.size() == 0) {
            return;
        }
        for (Iterator<ConcurrentHashMap.Entry<String, StreamObserver>> iterator = uniqueObserverMap.entrySet()
                .iterator(); iterator.hasNext();) {
            if (iterator.next().getKey().equals(uniqueId)) {
                iterator.remove();
                break;
            }
        }

    }

    public static void mapClear(String requestId) {
        REQUEST_LATCH_MAP.remove(requestId);
        REQUEST_MAP.remove(requestId);
    }

    public static Map<String, ConcurrentHashMap<String, StreamObserver>> getStreamMap() {
        return STREAM_OBSERVER_MAP;
    }

    @Override
    public StreamObserver<RpcCallBackRequest> call(
            final StreamObserver<RpcCallBackResponse> responseObserver) {
        return new StreamObserver<RpcCallBackRequest>() {
            @Override
            public void onNext(final RpcCallBackRequest rpcCallBackRequest) {
                try {

                    String requestId = rpcCallBackRequest.getRequestId();
                    String appNameUnique = rpcCallBackRequest.getAppName();
                    if (Constants.HEARTBEAT_TAG == rpcCallBackRequest.getHeartBeatTag()) {// 心跳上报
                        LOGGER.debug("Receive heartbeat from {}.", rpcCallBackRequest.getAppName());
                        handleHeartBeat(rpcCallBackRequest.getAppName(), responseObserver);
                    }
                    if (Constants.REGISTER == rpcCallBackRequest.getHeartBeatTag()) { // 注册连接
                        LOGGER.info("RpcClient: {} has connected success! ", rpcCallBackRequest.getAppName());
                        appNameUnique = StringTools.makeRegisterAppName(appNameUnique);
                        handleRegister(appNameUnique, InetUtil.getHostIp(), responseObserver);
                    } else {// 正常请求
                        handleCallBackResponse(requestId, rpcCallBackRequest);
                    }
                } catch (Throwable e) {
                    LOGGER.error("", e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.info("RpcServer onError!  responseObserver:{}", responseObserver);
                // 客户端断开，清理本地缓存的连接
                refreshLocalAvailableObserver(responseObserver);
            }

            @Override
            public void onCompleted() {
                // 客户端心跳长时间没有response，会将stream 关闭
                LOGGER.info("RpcServer onCompleted!  responseObserver:{}", responseObserver);
                refreshLocalAvailableObserver(responseObserver);
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * 处理注册连接请求
     *
     * @param appNameUnique
     * @param hostIp
     * @param responseObserver
     */
    private void handleRegister(String appNameUnique, String hostIp,
            StreamObserver<RpcCallBackResponse> responseObserver) {
        try {
            // onNext请求之后，再添加到本地，防止responseObserver并发问题
            remoteRegister(appNameUnique, hostIp);
            responseObserver.onNext(
                    RpcCallBackResponse.newBuilder().setAppName(appNameUnique).setRegisterResult(true).build());
            addLocalAvailableStreamObserver(appNameUnique, responseObserver);
        } catch (Exception e) {
            // register fail,
            LOGGER.error("client register to server fail ", e);
            refreshLocalAvailableObserver(responseObserver);
            responseObserver.onError(Status.INTERNAL.asException());

        }
    }

    /**
     * 处理回调结果
     *
     * @param requestId
     * @param response
     */
    private void handleCallBackResponse(String requestId, RpcCallBackRequest response) {
        if (REQUEST_LATCH_MAP.containsKey(requestId)) {
            localResponse(requestId, response);
        } else if (REMOTE_SERVER_OBSERVER_MAP.containsKey(requestId)) {
            responseCallbackProxy(requestId, response);
        }
    }

    /**
     * 处理本地服务器发起的二阶段/回查的请求结果
     *
     * @param requestId
     * @param response
     */
    private void localResponse(String requestId, RpcCallBackRequest response) {
        CountDownLatch finishLatch = REQUEST_LATCH_MAP.get(requestId);
        REQUEST_MAP.putIfAbsent(requestId, response);
        finishLatch.countDown();
    }

    /**
     * 处理远程sc服务器发起的二阶段/回查的请求结果，作为proxy异步返回
     *
     * @param requestId
     * @param response
     */
    private void responseCallbackProxy(String requestId, RpcCallBackRequest response) {
        StreamObserver<RpcCallBackRequest> observer = REMOTE_SERVER_OBSERVER_MAP.get(requestId);
        REMOTE_SERVER_OBSERVER_MAP.remove(requestId);
        try {
            LOGGER.debug("Response proxy request : {}", response);
            observer.onNext(response);
            observer.onCompleted();
        } catch (StatusRuntimeException e) {
            LOGGER.warn("Response to remote server failed. ", e);
        }
    }

    /**
     * 处理心跳，进行连接信息续租
     *
     * @param appNameUnique
     */
    private void handleHeartBeat(String appNameUnique, StreamObserver<RpcCallBackResponse> responseObserver) {
        CallBackStreamExecutors.getInstance().callback(responseObserver,
                RpcCallBackResponse.newBuilder().setAppName(appNameUnique).setHeartBeatResult(true).build());

    }

    /**
     * 去远端注册连接信息，用于管理形如key=/sc/grpc/{appname}/{uuid}，value=hostIp的连接关系
     *
     * @param appNameUnique
     * @param hostIp
     *             写入超时，抛出运行时异常
     */
    private void remoteRegister(String appNameUnique, String hostIp) {
        ZookeeperRegistryCenter.getInstance().persistEphemeral(appNameUnique, hostIp);
    }
}
