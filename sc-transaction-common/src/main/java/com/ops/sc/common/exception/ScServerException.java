package com.ops.sc.common.exception;

import com.ops.sc.common.enums.TransactionResponseCode;


public class ScServerException extends RuntimeException {

    private TransactionResponseCode transactionResponseCode;


    public ScServerException(TransactionResponseCode transactionResponseCode, String message){
        super(message);
        this.transactionResponseCode = transactionResponseCode;
    }

    public ScServerException(TransactionResponseCode transactionResponseCode, String args, Throwable cause) {
        super(args,cause);
        this.transactionResponseCode = transactionResponseCode;
    }

    public TransactionResponseCode getServerResponseErrorCode() {
        return transactionResponseCode;
    }

}
