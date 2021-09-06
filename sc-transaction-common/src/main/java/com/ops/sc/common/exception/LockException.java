package com.ops.sc.common.exception;

import java.sql.SQLException;


public class LockException extends SQLException {

    public LockException() {
    }

    public LockException(String msg) {
        super(msg);
    }
}
