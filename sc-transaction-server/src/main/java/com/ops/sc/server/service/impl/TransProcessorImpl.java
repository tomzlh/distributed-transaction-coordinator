package com.ops.sc.server.service.impl;

import javax.annotation.Resource;

import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.common.trans.BaseTwoPhaseTransaction;
import com.ops.sc.common.trans.CommonTransInfo;
import com.ops.sc.common.trans.TransCommonResponse;
import com.ops.sc.rpc.dto.*;
import com.ops.sc.server.service.TransactionProcessor;
import com.ops.sc.server.service.TransactionStatusService;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service("transProcessorImpl")
public class TransProcessorImpl extends TransactionProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransProcessorImpl.class);

    @Resource(name = "globalTwoPhaseTransaction")
    private BaseTwoPhaseTransaction globalTwoPhaseTransaction;

    @Resource
    private TransactionStatusService transactionStatusService;


    @Override
    public void startGlobalTrans(final GlobalTransRequest request, StreamObserver responseObserver) {
        transactionStatusService.processGlobalTrans(request,responseObserver);
    }

    @Override
    public void startGlobalTrans(GlobalSagaTransRequest request, StreamObserver responseObserver) {
        transactionStatusService.processGlobalSagaTrans(request,responseObserver);
    }

    @Override
    public void rollbackGlobalTrans(GlobalTransRollbackRequest request, StreamObserver responseObserver) throws ScTransactionException {
        if(TransMode.valueOf(request.getTransMode())==TransMode.SAGA) {
            transactionStatusService.processGlobalSagaRollback(request, responseObserver);
        }
        else if(TransMode.valueOf(request.getTransMode())==TransMode.XA){
            transactionStatusService.processGlobalRollback(request, responseObserver);
        }
        else if(TransMode.valueOf(request.getTransMode())==TransMode.TCC){
            transactionStatusService.processGlobalRollback(request, responseObserver);
        }
    }



    @Override
    protected TransCommonResponse executeGlobalCommit(CommonTransInfo commonTransInfo) {
        return globalTwoPhaseTransaction.commit(commonTransInfo);
    }

    @Override
    protected TransCommonResponse executeGlobalRollback(CommonTransInfo commonTransInfo) {
        return globalTwoPhaseTransaction.rollback(commonTransInfo);
    }
}
