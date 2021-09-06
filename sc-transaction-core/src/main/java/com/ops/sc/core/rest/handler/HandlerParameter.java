
package com.ops.sc.core.rest.handler;

import com.ops.sc.core.rest.enums.ParamPart;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Describe parameters of a handle method.
 */
@RequiredArgsConstructor
@Getter
public final class HandlerParameter {
    
    private final int index;
    
    private final Class<?> type;
    
    private final ParamPart paramPart;
    
    private final String name;
    
    private final boolean required;
}
