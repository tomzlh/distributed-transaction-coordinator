
package com.ops.sc.common.reg.exception;


public final class RegException extends RuntimeException {
    
    private static final long serialVersionUID = -6417179023552012152L;
    
    public RegException(final Exception cause) {
        super(cause);
    }
}
