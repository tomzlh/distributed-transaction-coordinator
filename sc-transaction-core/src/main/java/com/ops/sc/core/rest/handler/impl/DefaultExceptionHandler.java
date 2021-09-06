
package com.ops.sc.core.rest.handler.impl;

import com.ops.sc.core.rest.Http;
import com.ops.sc.core.rest.handler.ExceptionHandler;
import com.ops.sc.core.rest.handler.ExceptionHandleResult;
import io.netty.handler.codec.http.HttpResponseStatus;


/**
 * A default handler for handling {@link Throwable}.
 */
public final class DefaultExceptionHandler implements ExceptionHandler<Throwable> {
    
    @Override
    public ExceptionHandleResult handleException(final Throwable ex) {
        return ExceptionHandleResult.builder()
                .statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .contentType(Http.DEFAULT_CONTENT_TYPE)
                .result(ex.getLocalizedMessage())
                .build();
    }
}
