
package com.ops.sc.core.rest.handler;

import com.ops.sc.core.rest.Http;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class ExceptionHandleResult {
    
    private final Object result;
    
    @Builder.Default
    private final int statusCode = 500;
    
    @Builder.Default
    private final String contentType = Http.DEFAULT_CONTENT_TYPE;
}
