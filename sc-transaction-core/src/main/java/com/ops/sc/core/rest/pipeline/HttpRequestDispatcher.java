
package com.ops.sc.core.rest.pipeline;

import com.ops.sc.core.rest.handler.HandleContext;
import com.ops.sc.core.rest.handler.Handler;
import com.ops.sc.core.rest.handler.HandlerMappingRegistry;
import com.ops.sc.core.rest.handler.HandlerNotFoundException;
import com.ops.sc.core.rest.mapping.MappingContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;


@Sharable
@Slf4j
public final class HttpRequestDispatcher extends ChannelInboundHandlerAdapter {
    
    private static final String TRAILING_SLASH = "/";

    
    private final boolean trailingSlashSensitive;
    
    public HttpRequestDispatcher(final boolean trailingSlashSensitive) {
        this.trailingSlashSensitive = trailingSlashSensitive;
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        log.debug("{}", msg);
        FullHttpRequest request = (FullHttpRequest) msg;
        if (!trailingSlashSensitive) {
            request.setUri(appendTrailingSlashIfAbsent(request.uri()));
        }
        MappingContext<Handler> mappingContext = HandlerMappingRegistry.getInstance().getMappingContext(request);
        if (null == mappingContext) {
            ReferenceCountUtil.release(request);
            throw new HandlerNotFoundException(request.uri());
        }
        HandleContext<Handler> handleContext = new HandleContext<>(request, mappingContext);
        ctx.fireChannelRead(handleContext);
    }
    

    

    private String appendTrailingSlashIfAbsent(final String uri) {
        String[] split = uri.split("\\?");
        if (1 == split.length) {
            return uri.endsWith(TRAILING_SLASH) ? uri : uri + TRAILING_SLASH;
        }
        String path = split[0];
        return path.endsWith(TRAILING_SLASH) ? uri : path + TRAILING_SLASH + "?" + split[1];
    }
}
