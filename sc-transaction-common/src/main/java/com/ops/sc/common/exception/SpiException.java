package com.ops.sc.common.exception;


public class SpiException extends RuntimeException {

    public SpiException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpiException(String message) {
        super(message);
    }

    public SpiException(Throwable cause) {
        super(cause);
    }

    public SpiException() {
        super();
    }
}
