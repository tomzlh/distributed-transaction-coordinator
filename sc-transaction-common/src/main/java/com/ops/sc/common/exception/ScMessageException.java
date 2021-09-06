package com.ops.sc.common.exception;


import com.ops.sc.common.enums.ClientErrorCode;
import org.springframework.core.NestedRuntimeException;

public class ScMessageException extends NestedRuntimeException {

    private ClientErrorCode clientErrorCode;

    public ScMessageException(ClientErrorCode clientErrorCode, Throwable cause) {
        super(clientErrorCode.getErrorCode(), cause);
        this.clientErrorCode = clientErrorCode;
    }

    public ScMessageException(ClientErrorCode clientErrorCode, String msg) {
        super(clientErrorCode.getErrorCode() + ": " + msg);
        this.clientErrorCode = clientErrorCode;
    }

    public ScMessageException(ClientErrorCode clientErrorCode, String msg, Throwable cause) {
        super(clientErrorCode.getErrorCode() + ": " + msg, cause);
        this.clientErrorCode = clientErrorCode;
    }

    public ClientErrorCode getClientErrorCode() {
        return clientErrorCode;
    }

}
