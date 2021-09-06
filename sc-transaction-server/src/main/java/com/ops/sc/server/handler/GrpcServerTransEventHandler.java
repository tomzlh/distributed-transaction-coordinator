package com.ops.sc.server.handler;

import com.lmax.disruptor.EventHandler;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.model.TransactionInfo;
import com.ops.sc.common.trans.TransCommonResponse;
import com.ops.sc.core.build.RpcResponseBuilder;
import com.ops.sc.core.util.ApplicationUtils;
import com.ops.sc.rpc.dto.GlobalTransResponse;
import com.ops.sc.server.transaction.TransactionProcessorFactory;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GrpcServerTransEventHandler implements EventHandler<TransactionInfo> {

    private Logger logger = LoggerFactory.getLogger(GrpcServerTransEventHandler.class);

    @Override
    public void onEvent(TransactionInfo transactionInfo, long sequence, boolean endOfBatch) throws Exception{
        logger.info("start to start the transaction : {}",transactionInfo);
        TransactionProcessorFactory transactionProcessorFactory= ApplicationUtils.getBean("transactionProcessorFactory", TransactionProcessorFactory.class);
        process(transactionProcessorFactory,transactionInfo);
    }


    private void process(TransactionProcessorFactory transactionProcessorFactory, TransactionInfo transactionInfo){
        StreamObserver streamObserver = transactionInfo.getStreamObserver();
        try {
            TransCommonResponse transCommonResponse = transactionProcessorFactory.processGlobalTrans(transactionInfo);
            if (transCommonResponse.isSuccess()) {
                streamObserver.onNext(buildSuccessResponse(transactionInfo.getTid(),transactionInfo.getBusinessId()));
            } else {
                streamObserver
                        .onNext(buildFailedResponse(transactionInfo.getTid(),transactionInfo.getBusinessId(),transCommonResponse.getErrorMsg()));
            }
        }catch (Throwable e) {
            logger.error("Execute tcc transaction error:{}",transactionInfo,e);
            streamObserver.onNext(buildFailedResponse(transactionInfo.getTid(),transactionInfo.getBusinessId(),e.getMessage()));
        }finally {
            streamObserver.onCompleted();
        }
    }


    private GlobalTransResponse buildSuccessResponse(long tid,String businessId){
        GlobalTransResponse.Builder resultBuilder = GlobalTransResponse.newBuilder();
        resultBuilder.setTid(String.valueOf(tid));
        resultBuilder.setBaseResponse(RpcResponseBuilder.buildSuccessBaseResponse(businessId));
        return resultBuilder.build();
    }

    private GlobalTransResponse buildFailedResponse(long tid,String businessId,String errorMsg){
        GlobalTransResponse.Builder resultBuilder = GlobalTransResponse.newBuilder();
        resultBuilder.setTid(String.valueOf(tid));
        resultBuilder.setBaseResponse(RpcResponseBuilder.buildErrorBaseResponse(TransactionResponseCode.TRANSACTION_PROCESS_FAILED,businessId,errorMsg));
        return resultBuilder.build();
    }

}
