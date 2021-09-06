
package com.ops.sc.core.rest.pipeline;

import com.ops.sc.core.rest.config.RpcServiceConfiguration;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public final class RestfulServiceChannelInitializer extends ChannelInitializer<Channel> {
    
    private final HttpRequestDispatcher httpRequestDispatcher;
    
    private final HandlerParameterDecoder handlerParameterDecoder;
    
    private final HandleMethodExecutor handleMethodExecutor;
    
    private final ExceptionHandling exceptionHandling;
    
    public RestfulServiceChannelInitializer() {
        httpRequestDispatcher = new HttpRequestDispatcher(RpcServiceConfiguration.isTrailingSlashSensitive());
        handlerParameterDecoder = new HandlerParameterDecoder();
        handleMethodExecutor = new HandleMethodExecutor();
        exceptionHandling = new ExceptionHandling(RpcServiceConfiguration.getExceptionHandlers());
    }
    
    @Override
    protected void initChannel(final Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast("dispatcher", httpRequestDispatcher);
        pipeline.addLast("handlerParameterDecoder", handlerParameterDecoder);
        pipeline.addLast("handleMethodExecutor", handleMethodExecutor);
        pipeline.addLast("exceptionHandling", exceptionHandling);
    }
}
