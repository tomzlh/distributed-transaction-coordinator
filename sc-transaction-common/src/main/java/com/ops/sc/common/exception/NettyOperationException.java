package com.ops.sc.common.exception;

import com.ops.sc.common.enums.CallErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyOperationException extends RuntimeException{

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyOperationException.class);

    private static final long serialVersionUID = 5531074229174745826L;

    private final CallErrorCode errorCode;


    public NettyOperationException() {
        this(CallErrorCode.UnknownAppError);
    }


    public NettyOperationException(CallErrorCode err) {
        this(err.getErrorCode(), err);
    }


    public NettyOperationException(String msg) {
        this(msg, CallErrorCode.UnknownAppError);
    }


    public NettyOperationException(String msg, CallErrorCode errCode) {
        this(null, msg, errCode);
    }


    public NettyOperationException(Throwable cause, String msg, CallErrorCode errCode) {
        super(msg, cause);
        this.errorCode = errCode;
    }


    public NettyOperationException(Throwable th) {
        this(th, th.getMessage());
    }


    public NettyOperationException(Throwable th, String msg) {
        this(th, msg, CallErrorCode.UnknownAppError);
    }


    public CallErrorCode getErrorCode() {
        return errorCode;
    }


    public static NettyOperationException nestedException(Throwable e) {
        return nestedException("", e);
    }


    public static NettyOperationException nestedException(String msg, Throwable e) {
        LOGGER.error(msg, e.getMessage(), e);
        if (e instanceof NettyOperationException) {
            return (NettyOperationException)e;
        }

        return new NettyOperationException(e, msg);
    }

}
