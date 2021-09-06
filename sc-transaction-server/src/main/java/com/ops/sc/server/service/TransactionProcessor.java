package com.ops.sc.server.service;

import com.ops.sc.common.exception.RpcException;
import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.common.trans.CommonTransInfo;
import com.ops.sc.common.trans.TransCommonResponse;
import com.ops.sc.rpc.dto.*;
import io.grpc.stub.StreamObserver;

public abstract class TransactionProcessor {

    public abstract void startGlobalTrans(final GlobalTransRequest request, StreamObserver responseObserver) throws
            ScTransactionException;

    public abstract void startGlobalTrans(final GlobalSagaTransRequest request, StreamObserver responseObserver) throws
            ScTransactionException;

    public abstract void rollbackGlobalTrans(final GlobalTransRollbackRequest request, StreamObserver responseObserver) throws
            ScTransactionException;


    protected void beforeGlobalCommit() {}

    protected void afterGlobalCommit() {}

    protected void beforeGlobalRollBack() {}

    protected void afterGlobalRollBack() {}

    public final TransCommonResponse commitGlobalTrans(CommonTransInfo commonTransInfo) {
        beforeGlobalCommit();
        TransCommonResponse result = executeGlobalCommit(commonTransInfo);
        afterGlobalCommit();
        return result;
    }

    public final TransCommonResponse rollBackGlobalTrans(CommonTransInfo commonTransInfo) {
        beforeGlobalRollBack();
        TransCommonResponse result = executeGlobalRollback(commonTransInfo);
        afterGlobalRollBack();
        return result;
    }

    protected abstract TransCommonResponse executeGlobalCommit(CommonTransInfo commonTransInfo);

    protected abstract TransCommonResponse executeGlobalRollback(CommonTransInfo commonTransInfo);


}
