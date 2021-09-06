package com.ops.sc.common.exception;


public class ResourceException extends Exception {

    public ResourceException(String msg) {
        super(msg);
    }

    public ResourceException(Throwable cause) {
        super(cause);
    }

    public ResourceException(String msg,Throwable cause) {
        super(msg,cause);
    }
}
