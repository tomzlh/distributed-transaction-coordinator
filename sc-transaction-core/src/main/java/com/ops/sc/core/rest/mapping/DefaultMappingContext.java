
package com.ops.sc.core.rest.mapping;

import lombok.RequiredArgsConstructor;

/**
 * Default mapping context.
 *
 * @param <T> Type of payload
 */
@RequiredArgsConstructor
public final class DefaultMappingContext<T> implements MappingContext<T> {
    
    private final String pattern;
    
    private final T payload;
    
    @Override
    public String pattern() {
        return pattern;
    }
    
    @Override
    public T payload() {
        return payload;
    }
}
