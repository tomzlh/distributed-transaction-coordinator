package com.ops.sc.server.transaction;


public abstract class NativeTransMsgOperation extends TransMsgOperation {


    public abstract boolean nativeCommit(String token);


    public abstract boolean nativeRollback(String token);
}
