package com.ops.sc.common.reg.etcd;


public class EtcdException extends RuntimeException {

    public EtcdException(String message, Throwable cause) {
        super(message, cause);
    }

    public EtcdException(String message) {
        super(message);
    }

    public EtcdException(Throwable cause) {
        super(cause);
    }

    public EtcdException() {
        super();
    }

}
