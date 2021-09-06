package com.ops.sc.ta.clone.rollback;


public class ImageNotConsistentException extends RuntimeException {
    public ImageNotConsistentException(String msg) {
        super(msg);
    }
}
