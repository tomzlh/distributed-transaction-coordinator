
package com.ops.sc.core.rest.handler;

import java.text.MessageFormat;

public final class HandlerNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 7316145545440327554L;
    
    public HandlerNotFoundException(final String path) {
        super(MessageFormat.format("No handler found for [{0}].", path));
    }
}
