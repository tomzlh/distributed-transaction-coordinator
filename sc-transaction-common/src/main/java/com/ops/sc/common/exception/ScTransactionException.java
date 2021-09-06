package com.ops.sc.common.exception;


import com.ops.sc.common.enums.TransactionResponseCode;

public class ScTransactionException extends RuntimeException {

    private TransactionResponseCode transactionResponseCode;

    public ScTransactionException(String msg) {
        super(msg);
    }

    public ScTransactionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ScTransactionException(TransactionResponseCode transactionResponseCode, String msg) {
        super(transactionResponseCode.getCode() + ": " + msg);
        this.transactionResponseCode = transactionResponseCode;
    }

    public ScTransactionException(Throwable cause) {
        super("", cause);
    }
}
