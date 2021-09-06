package com.ops.sc.server.service;


import com.ops.sc.rpc.dto.*;
import io.grpc.stub.StreamObserver;

/**
 * 事务相关业务处理
 *
 */
public interface TransactionStatusService {

    BranchTransResponse processBranchTrans(final BranchTransRequest request);


    void processGlobalTrans(final GlobalTransRequest request, StreamObserver responseObserver);


    void processGlobalSagaTrans(GlobalSagaTransRequest request,StreamObserver responseObserver);


    void processGlobalSagaRollback(GlobalTransRollbackRequest request,StreamObserver responseObserver);


    void processGlobalRollback(GlobalTransRollbackRequest request,StreamObserver responseObserver);


    TransQueryResponse findGlobalTrans(TransQueryRequest request);

    /**
     * 发送混合事务消息
     *
     * @param request
     * @return
     */
    RegTransMsgResponse prepareTransMsg(final RegTransMsgRequest request);

    /**
     * 生产者注册
     *
     * @param request
     * @return
     */
    MQProducerRegResponse registerProducer(final MQProducerRegRequest request);

    /**
     * 本地模式StatCheck
     *
     * @param request
     * @return
     */
    StateServiceResponse stateCheck(final StateServiceRequest request);

    TransCompensationResponse transactionCompensate(final TransCompensationRequest request);


}
