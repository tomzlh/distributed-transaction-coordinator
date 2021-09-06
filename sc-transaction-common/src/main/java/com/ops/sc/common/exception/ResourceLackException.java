package com.ops.sc.common.exception;


public class ResourceLackException extends Exception {

    public ResourceLackException(String msg) {
        super(msg);
    }

    public ResourceLackException(Throwable cause) {
        super(cause);
    }
}
