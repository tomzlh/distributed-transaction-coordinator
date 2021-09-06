
package com.ops.sc.core.rest.handler.impl;

import com.ops.sc.core.rest.Http;
import com.ops.sc.core.rest.handler.ExceptionHandler;
import com.ops.sc.core.rest.handler.ExceptionHandleResult;
import com.ops.sc.core.rest.handler.HandlerNotFoundException;
import io.netty.handler.codec.http.HttpResponseStatus;


public final class DefaultHandlerNotFoundExceptionHandler implements ExceptionHandler<HandlerNotFoundException> {
    
    @Override
    public ExceptionHandleResult handleException(final HandlerNotFoundException ex) {
        return ExceptionHandleResult.builder()
                .statusCode(HttpResponseStatus.NOT_FOUND.code())
                .result(ex.getLocalizedMessage())
                .contentType(Http.DEFAULT_CONTENT_TYPE)
                .build();
    }
}
