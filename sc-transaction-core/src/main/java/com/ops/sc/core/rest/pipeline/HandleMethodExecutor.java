
package com.ops.sc.core.rest.pipeline;

import com.ops.sc.core.rest.handler.HandleContext;
import com.ops.sc.core.rest.serializer.ResponseBodySerializer;
import com.ops.sc.core.rest.serializer.ResponseBodySerializerFactory;
import com.ops.sc.core.rest.handler.Handler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;

import java.lang.reflect.InvocationTargetException;

/**
 * The handler which actually executes handle method and creates HTTP response for responding.
 * If an exception occurred when executing handle method, this handler would pass it to Handler named {@link ExceptionHandling}.
 */
@Sharable
public final class HandleMethodExecutor extends ChannelInboundHandlerAdapter {
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        HandleContext<Handler> handleContext = (HandleContext<Handler>) msg;
        try {
            Handler handler = handleContext.getMappingContext().payload();
            Object[] args = handleContext.getArgs();
            Object handleResult = handler.execute(args);
            FullHttpResponse response;
            if (null != handleResult) {
                String mimeType = HttpUtil.getMimeType(handler.getProducing()).toString();
                ResponseBodySerializer serializer = ResponseBodySerializerFactory.getResponseBodySerializer(mimeType);
                byte[] bodyBytes = serializer.serialize(handleResult);
                response = createHttpResponse(handler.getProducing(), bodyBytes, handler.getHttpStatusCode());
            } else {
                response = createHttpResponse(handler.getProducing(), new byte[0], handler.getHttpStatusCode());
            }
            ctx.writeAndFlush(response);
        } finally {
            ReferenceCountUtil.release(handleContext.getHttpRequest());
        }
    }
    
    private FullHttpResponse createHttpResponse(final String producingContentType, final byte[] bodyBytes, final int statusCode) {
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(statusCode);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus, Unpooled.wrappedBuffer(bodyBytes));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, producingContentType);
        HttpUtil.setContentLength(response, bodyBytes.length);
        HttpUtil.setKeepAlive(response, true);
        return response;
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (cause instanceof InvocationTargetException) {
            ctx.fireExceptionCaught(cause.getCause());
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }
}
