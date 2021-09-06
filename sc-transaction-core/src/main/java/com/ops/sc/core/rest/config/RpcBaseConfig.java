
package com.ops.sc.core.rest.config;

import com.ops.sc.core.config.NettyPropertyResolver;
import io.netty.channel.Channel;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueDomainSocketChannel;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.PlatformDependent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RpcBaseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcBaseConfig.class);

    /**
     * The constant BOSS_THREAD_PREFIX.
     */
    protected static final String BOSS_THREAD_PREFIX = NettyPropertyResolver.getINSTANCE().getValue(NettyConstants.BOSS_THREAD_PREFIX);

    /**
     * The constant WORKER_THREAD_PREFIX.
     */
    protected static final String WORKER_THREAD_PREFIX =  NettyPropertyResolver.getINSTANCE().getValue(NettyConstants.WORKER_THREAD_PREFIX);

    /**
     * The constant SHARE_BOSS_WORKER.
     */
    protected static final boolean SHARE_BOSS_WORKER =  NettyPropertyResolver.getINSTANCE().getBoolean(NettyConstants.SHARE_BOSS_WORKER);

    /**
     * The constant WORKER_THREAD_SIZE.
     */
    protected static int WORKER_THREAD_SIZE;

    /**
     * The constant TRANSPORT_SERVER_TYPE.
     */
    protected static final NetWorkServerType TRANSPORT_SERVER_TYPE;

    /**
     * The constant SERVER_CHANNEL_CLAZZ.
     */
    protected static  Class<? extends ServerChannel> SERVER_CHANNEL_CLAZZ;
    /**
     * The constant CLIENT_CHANNEL_CLAZZ.
     */
    protected static  Class<? extends Channel> CLIENT_CHANNEL_CLAZZ;

    /**
     * The constant TRANSPORT_PROTOCOL_TYPE.
     */
    protected static final NetWorkProType NETWORK_PROTOCOL_TYPE;

    private static final int DEFAULT_WRITE_IDLE_SECONDS = 5;

    private static final int READIDLE_BASE_WRITEIDLE = 3;


    private static final int DEFAULT_WORK_SIZE=Runtime.getRuntime().availableProcessors()+1;

    /**
     * The constant MAX_WRITE_IDLE_SECONDS.
     */
    protected static final int MAX_WRITE_IDLE_SECONDS;

    /**
     * The constant MAX_READ_IDLE_SECONDS.
     */
    protected static final int MAX_READ_IDLE_SECONDS;

    /**
     * The constant MAX_ALL_IDLE_SECONDS.
     */
    protected static final int MAX_ALL_IDLE_SECONDS = 0;

    static {
        NETWORK_PROTOCOL_TYPE = NetWorkProType.getType(NettyPropertyResolver.getINSTANCE().getValue(NettyConstants.TRANSPORT_TYPE, NetWorkProType.TCP.name()));
        String workerThreadSize = NettyPropertyResolver.getINSTANCE().getValue(NettyConstants.WORKER_THREAD_SIZE);
        if (StringUtils.isNotBlank(workerThreadSize) && StringUtils.isNumeric(workerThreadSize)) {
            WORKER_THREAD_SIZE = Integer.parseInt(workerThreadSize);
        } else {
            WORKER_THREAD_SIZE = DEFAULT_WORK_SIZE;
        }
        TRANSPORT_SERVER_TYPE = NetWorkServerType.getType(NettyPropertyResolver.getINSTANCE().getValue(NettyConstants.TRANSPORT_SERVER, NetWorkServerType.NIO.name()));
        switch (TRANSPORT_SERVER_TYPE) {
            case NIO:
                processNioType();
                break;
            case NATIVE:
                processNativeType();
                break;
            default:
                throw new IllegalArgumentException("unsupported.");
        }
        boolean enableHeartbeat = NettyPropertyResolver.getINSTANCE().getBoolean(NettyConstants.TRANSPORT_HEARTBEAT);
        if (enableHeartbeat) {
            MAX_WRITE_IDLE_SECONDS = DEFAULT_WRITE_IDLE_SECONDS;
        } else {
            MAX_WRITE_IDLE_SECONDS = 0;
        }
        MAX_READ_IDLE_SECONDS = MAX_WRITE_IDLE_SECONDS * READIDLE_BASE_WRITEIDLE;
    }

    private static void processNativeType() {
        if (PlatformDependent.isWindows()) {
            throw new IllegalArgumentException("no native supporting for Windows.");
        } else if (PlatformDependent.isOsx()) {
            if (NETWORK_PROTOCOL_TYPE == NetWorkProType.TCP) {
                SERVER_CHANNEL_CLAZZ = KQueueServerSocketChannel.class;
                CLIENT_CHANNEL_CLAZZ = KQueueSocketChannel.class;
            } else if (NETWORK_PROTOCOL_TYPE == NetWorkProType.UNIX_DOMAIN_SOCKET) {
                SERVER_CHANNEL_CLAZZ = KQueueServerDomainSocketChannel.class;
                CLIENT_CHANNEL_CLAZZ = KQueueDomainSocketChannel.class;
            } else {
                raiseUnsupportedTransportError();
                SERVER_CHANNEL_CLAZZ = null;
                CLIENT_CHANNEL_CLAZZ = null;
            }
        } else {
            if (NETWORK_PROTOCOL_TYPE == NetWorkProType.TCP) {
                SERVER_CHANNEL_CLAZZ = EpollServerSocketChannel.class;
                CLIENT_CHANNEL_CLAZZ = EpollSocketChannel.class;
            } else if (NETWORK_PROTOCOL_TYPE == NetWorkProType.UNIX_DOMAIN_SOCKET) {
                SERVER_CHANNEL_CLAZZ = EpollServerDomainSocketChannel.class;
                CLIENT_CHANNEL_CLAZZ = EpollDomainSocketChannel.class;
            } else {
                raiseUnsupportedTransportError();
                SERVER_CHANNEL_CLAZZ = null;
                CLIENT_CHANNEL_CLAZZ = null;
            }
        }
    }

    private static void processNioType() {
        if (NETWORK_PROTOCOL_TYPE == NetWorkProType.TCP) {
            SERVER_CHANNEL_CLAZZ = NioServerSocketChannel.class;
            CLIENT_CHANNEL_CLAZZ = NioSocketChannel.class;
        } else {
            raiseUnsupportedTransportError();
            SERVER_CHANNEL_CLAZZ = null;
            CLIENT_CHANNEL_CLAZZ = null;
        }
    }

    private static void raiseUnsupportedTransportError() throws RuntimeException {
        String errMsg = String.format("Unsupported provider type :[%s] for transport:[%s].", TRANSPORT_SERVER_TYPE,
                NETWORK_PROTOCOL_TYPE);
        LOGGER.error(errMsg);
        throw new IllegalArgumentException(errMsg);
    }


}
