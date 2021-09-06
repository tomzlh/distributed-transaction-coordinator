package com.ops.sc.common.classloader;

public class ServiceClassNotFoundException extends RuntimeException{

    private static final long serialVersionUID = 7848438418918409019L;


    public ServiceClassNotFoundException(String errorCode) {
        super(errorCode);
    }

    public ServiceClassNotFoundException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }


    public ServiceClassNotFoundException(String errorCode, String errorDesc) {
        super(errorCode + ":" + errorDesc);
    }


    public ServiceClassNotFoundException(String errorCode, String errorDesc, Throwable cause) {
        super(errorCode + ":" + errorDesc, cause);
    }

    /**
     * Instantiates a new Enhanced service not found exception.
     *
     * @param cause the cause
     */
    public ServiceClassNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
