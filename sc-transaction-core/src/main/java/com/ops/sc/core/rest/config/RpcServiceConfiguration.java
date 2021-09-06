
package com.ops.sc.core.rest.config;

import com.google.common.base.Preconditions;
import com.ops.sc.core.rest.handler.ExceptionHandler;
import com.ops.sc.core.resolver.ServicePropertyResolver;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


import java.util.*;


@Getter
@RequiredArgsConstructor
public final class RpcServiceConfiguration extends RpcBaseConfig {

    private static int serverSelectorThreads = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.TRANSPORT_PREFIX + "serverSelectorThreads", String.valueOf(WORKER_THREAD_SIZE)));
    private static int serverSocketSendBufSize = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.TRANSPORT_PREFIX + "serverSocketSendBufSize", String.valueOf(153600)));
    private static int serverSocketResvBufSize = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.TRANSPORT_PREFIX + "serverSocketResvBufSize", String.valueOf(153600)));
    private static int serverWorkerThreads = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.TRANSPORT_PREFIX + "serverWorkerThreads", String.valueOf(WORKER_THREAD_SIZE)));
    private static int soBackLogSize = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.TRANSPORT_PREFIX + "soBackLogSize", String.valueOf(1024)));
    private static int writeBufferHighWaterMark = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.TRANSPORT_PREFIX + "writeBufferHighWaterMark", String.valueOf(67108864)));
    private static int writeBufferLowWaterMark = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.TRANSPORT_PREFIX + "writeBufferLowWaterMark", String.valueOf(1048576)));
    private static final int DEFAULT_LISTEN_PORT = 8091;
    private static final int RPC_REQUEST_TIMEOUT = 30 * 1000;
    private int serverChannelMaxIdleTimeSeconds = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.TRANSPORT_PREFIX + "serverChannelMaxIdleTimeSeconds", String.valueOf(30)));
    private static final String EPOLL_WORKER_THREAD_PREFIX = "NettyServerEPollWorker";
    private static int minServerPoolSize = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.MIN_SERVER_POOL_SIZE, "50"));
    private static int maxServerPoolSize = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.MAX_SERVER_POOL_SIZE, "500"));
    private static int maxTaskQueueSize = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.MAX_TASK_QUEUE_SIZE, "20000"));
    private static int keepAliveTime = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.KEEP_ALIVE_TIME, "500"));


    private static int serverPort = Integer.parseInt(ServicePropertyResolver.getINSTANCE().getValue(
            NettyConstants.SERVER_PORT, "8888"));
    

    @Getter
    private static boolean trailingSlashSensitive;
    
    private final static List<Object> controllerInstances = new ArrayList<>();
    @Getter
    private final static Map<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> exceptionHandlers = new HashMap<>();
    
    /**
     * Add instances of RestfulController.
     *
     * @param instances instances of RestfulController
     */
    public static void addControllerInstance(final Object... instances) {
        controllerInstances.addAll(Arrays.asList(instances));
    }

    public static List<Object> getControllerInstances(){
        return controllerInstances;
    }
    

    public static <E extends Throwable>  void addExceptionHandler(final Class<E> exceptionType, final ExceptionHandler<E> exceptionHandler) {
        Preconditions.checkState(!exceptionHandlers.containsKey(exceptionType), "ExceptionHandler for %s has already existed.", exceptionType.getName());
        exceptionHandlers.put(exceptionType, exceptionHandler);
    }

    public static final Class<? extends ServerChannel> SERVER_CHANNEL_CLAZZ = RpcBaseConfig.SERVER_CHANNEL_CLAZZ;



    public static int getServerSelectorThreads() {
        return serverSelectorThreads;
    }

    /**
     * Sets server selector threads.
     *
     * @param serverSelectorThreads the server selector threads
     */
    public static void setServerSelectorThreads(int serverSelectorThreads) {
        RpcServiceConfiguration.serverSelectorThreads = serverSelectorThreads;
    }

    /**
     * Enable epoll boolean.
     *
     * @return the boolean
     */
    public static boolean enableEpoll() {
        return RpcBaseConfig.SERVER_CHANNEL_CLAZZ.equals(EpollServerSocketChannel.class)
                && Epoll.isAvailable();

    }

    /**
     * Gets server socket send buf size.
     *
     * @return the server socket send buf size
     */
    public static int getServerSocketSendBufSize() {
        return serverSocketSendBufSize;
    }

    /**
     * Sets server socket send buf size.
     *
     * @param serverSocketSendBufSize the server socket send buf size
     */
    public static void setServerSocketSendBufSize(int serverSocketSendBufSize) {
        RpcServiceConfiguration.serverSocketSendBufSize = serverSocketSendBufSize;
    }

    /**
     * Gets server socket resv buf size.
     *
     * @return the server socket resv buf size
     */
    public static int getServerSocketResvBufSize() {
        return serverSocketResvBufSize;
    }

    /**
     * Sets server socket resv buf size.
     *
     * @param serverSocketResvBufSize the server socket resv buf size
     */
    public static void setServerSocketResvBufSize(int serverSocketResvBufSize) {
        RpcServiceConfiguration.serverSocketResvBufSize = serverSocketResvBufSize;
    }

    /**
     * Gets server worker threads.
     *
     * @return the server worker threads
     */
    public static int getServerWorkerThreads() {
        return serverWorkerThreads;
    }

    /**
     * Sets server worker threads.
     *
     * @param serverWorkerThreads the server worker threads
     */
    public void setServerWorkerThreads(int serverWorkerThreads) {
        this.serverWorkerThreads = serverWorkerThreads;
    }

    /**
     * Gets so back log size.
     *
     * @return the so back log size
     */
    public static int getSoBackLogSize() {
        return soBackLogSize;
    }

    /**
     * Sets so back log size.
     *
     * @param soBackLogSize the so back log size
     */
    public void setSoBackLogSize(int soBackLogSize) {
        this.soBackLogSize = soBackLogSize;
    }

    /**
     * Gets write buffer high water mark.
     *
     * @return the write buffer high water mark
     */
    public static int getWriteBufferHighWaterMark() {
        return writeBufferHighWaterMark;
    }

    /**
     * Sets write buffer high water mark.
     *
     * @param writeBufferHighWaterMark the write buffer high water mark
     */
    public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
    }

    /**
     * Gets write buffer low water mark.
     *
     * @return the write buffer low water mark
     */
    public static int getWriteBufferLowWaterMark() {
        return writeBufferLowWaterMark;
    }

    /**
     * Sets write buffer low water mark.
     *
     * @param writeBufferLowWaterMark the write buffer low water mark
     */
    public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
    }

    /**
     * Gets listen port.
     *
     * @return the listen port
     */
    public static int getDefaultListenPort() {
        return DEFAULT_LISTEN_PORT;
    }

    /**
     * Gets channel max read idle seconds.
     *
     * @return the channel max read idle seconds
     */
    public static int getChannelMaxReadIdleSeconds() {
        return MAX_READ_IDLE_SECONDS;
    }

    /**
     * Gets server channel max idle time seconds.
     *
     * @return the server channel max idle time seconds
     */
    public int getServerChannelMaxIdleTimeSeconds() {
        return serverChannelMaxIdleTimeSeconds;
    }

    /**
     * Gets rpc request timeout.
     *
     * @return the rpc request timeout
     */
    public static int getRpcRequestTimeout() {
        return RPC_REQUEST_TIMEOUT;
    }

    /**
     * Get boss thread prefix string.
     *
     * @return the string
     */
    public static String getBossThreadPrefix() {
        return ServicePropertyResolver.getINSTANCE().getValue(NettyConstants.BOSS_THREAD_PREFIX, "BOSS");
    }

    /**
     * Get worker thread prefix string.
     *
     * @return the string
     */
    public static String getWorkerThreadPrefix() {
        return ServicePropertyResolver.getINSTANCE().getValue(NettyConstants.WORKER_THREAD_PREFIX,
                enableEpoll() ? EPOLL_WORKER_THREAD_PREFIX : "WORKER");
    }

    /**
     * Get executor thread prefix string.
     *
     * @return the string
     */
    public static String getExecutorThreadPrefix() {
        return ServicePropertyResolver.getINSTANCE().getValue(NettyConstants.SERVER_EXECUTOR_THREAD_PREFIX,
                "nettyHandler");
    }

    /**
     * Get boss thread size int.
     *
     * @return the int
     */
    public static int getBossThreadSize() {
        return ServicePropertyResolver.getINSTANCE().getIntValue(NettyConstants.BOSS_THREAD_SIZE, 1);
    }

    /**
     * Get the timeout seconds of shutdown.
     *
     * @return the int
     */
    public static int getServerShutdownWaitTime() {
        return ServicePropertyResolver.getINSTANCE().getIntValue(NettyConstants.SHUTDOWN_WAIT, 3);
    }

    public static int getMinServerPoolSize() {
        return minServerPoolSize;
    }

    public static int getMaxServerPoolSize() {
        return maxServerPoolSize;
    }

    public static int getMaxTaskQueueSize() {
        return maxTaskQueueSize;
    }

    public static int getKeepAliveTime() {
        return keepAliveTime;
    }

    public static int getServerPort() {
        return serverPort;
    }
}
