
package com.ops.sc.core.rest;

import com.ops.sc.common.reg.RegConfFactory;
import com.ops.sc.common.thread.NamedThreadFactory;
import com.ops.sc.common.utils.InetUtil;
import com.ops.sc.core.rest.config.RpcServiceConfiguration;
import com.ops.sc.core.rest.pipeline.RestfulServiceChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public final class NettyServerFacade implements NettyFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerFacade.class);

    private ServerBootstrap serverBootstrap = new ServerBootstrap();
    private EventLoopGroup eventLoopGroupWorker;
    private EventLoopGroup eventLoopGroupBoss;
    private ChannelHandler[] channelHandlers;
    private int listenPort;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private static NettyServerFacade instance =NettyServerFacade.SingleInstanceHolder.instance;


    public static NettyServerFacade getInstance(){
        return instance;
    }
    
    private void initServerBootstrap() {
        if (RpcServiceConfiguration.enableEpoll()) {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(RpcServiceConfiguration.getBossThreadSize(),
                    new NamedThreadFactory(RpcServiceConfiguration.getBossThreadPrefix(), RpcServiceConfiguration.getBossThreadSize()));
            this.eventLoopGroupWorker = new EpollEventLoopGroup(RpcServiceConfiguration.getServerWorkerThreads(),
                    new NamedThreadFactory(RpcServiceConfiguration.getWorkerThreadPrefix(),
                            RpcServiceConfiguration.getServerWorkerThreads()));
        } else {
            this.eventLoopGroupBoss = new NioEventLoopGroup(RpcServiceConfiguration.getBossThreadSize(),
                    new NamedThreadFactory(RpcServiceConfiguration.getBossThreadPrefix(), RpcServiceConfiguration.getBossThreadSize()));
            this.eventLoopGroupWorker = new NioEventLoopGroup(RpcServiceConfiguration.getServerWorkerThreads(),
                    new NamedThreadFactory(RpcServiceConfiguration.getWorkerThreadPrefix(),
                            RpcServiceConfiguration.getServerWorkerThreads()));
        }
        listenPort = RpcServiceConfiguration.getServerPort();
    }
    
    @SneakyThrows
    @Override
    public void startup() {
        initServerBootstrap();
        this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupWorker)
                .channel(RpcServiceConfiguration.SERVER_CHANNEL_CLAZZ)
                .option(ChannelOption.SO_BACKLOG, RpcServiceConfiguration.getSoBackLogSize())
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, RpcServiceConfiguration.getServerSocketSendBufSize())
                .childOption(ChannelOption.SO_RCVBUF, RpcServiceConfiguration.getServerSocketResvBufSize())
                .localAddress(new InetSocketAddress(listenPort))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new IdleStateHandler(RpcServiceConfiguration.getChannelMaxReadIdleSeconds(), 0, 0))
                                .addLast(new RestfulServiceChannelInitializer());
                        if (channelHandlers != null) {
                            addChannelPipelineLast(ch, channelHandlers);
                        }

                    }
                });

        try {
            ChannelFuture future = this.serverBootstrap.bind(listenPort).sync();
            LOGGER.info("Server started, listen port: {}", listenPort);
            //RegConfFactory.getInstance().getRegistryService().register(new InetSocketAddress(InetUtil.getHostIp(),NettyServerConfig.getServerPort()));
            initialized.set(true);
            future.channel().closeFuture().sync();
        } catch (Exception exx) {
            throw new RuntimeException(exx);
        }
    }

    /**
     * Sets channel handlers.
     *
     * @param handlers the handlers
     */
    protected void setChannelHandlers(final ChannelHandler... handlers) {
        if (handlers != null) {
            channelHandlers = handlers;
        }
    }

    /**
     * Add channel pipeline last.
     *
     * @param channel  the channel
     * @param handlers the handlers
     */
    private void addChannelPipelineLast(Channel channel, ChannelHandler... handlers) {
        if (channel != null && handlers != null) {
            channel.pipeline().addLast(handlers);
        }
    }

    /**
     * Sets listen port.
     *
     * @param listenPort the listen port
     */
    public void setListenPort(int listenPort) {

        if (listenPort <= 0) {
            throw new IllegalArgumentException("listen port: " + listenPort + " is invalid!");
        }
        this.listenPort = listenPort;
    }

    /**
     * Gets listen port.
     *
     * @return the listen port
     */
    public int getListenPort() {
        return listenPort;
    }


    private static class SingleInstanceHolder {
        private static NettyServerFacade instance = new NettyServerFacade();
    }

    @Override
    public void shutdown() {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Shutting server down. ");
            }
            if (initialized.get()) {
                RegConfFactory.getInstance().getRegistryService().unregister(new InetSocketAddress(InetUtil.getHostIp(), RpcServiceConfiguration.getServerPort()));
                RegConfFactory.getInstance().getRegistryService().close();
                //wait a few seconds for server transport
                TimeUnit.SECONDS.sleep(RpcServiceConfiguration.getServerShutdownWaitTime());
            }

            this.eventLoopGroupBoss.shutdownGracefully();
            this.eventLoopGroupWorker.shutdownGracefully();
        } catch (Exception exx) {
            LOGGER.error(exx.getMessage());
        }
    }
}
