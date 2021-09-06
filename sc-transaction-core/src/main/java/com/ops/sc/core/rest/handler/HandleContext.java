
package com.ops.sc.core.rest.handler;

import com.ops.sc.core.rest.mapping.MappingContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;



@RequiredArgsConstructor
@Getter
@Setter
public final class HandleContext<T> {
    
    private final FullHttpRequest httpRequest;
    
    private final MappingContext<T> mappingContext;
    
    private Object[] args = new Object[0];
}
