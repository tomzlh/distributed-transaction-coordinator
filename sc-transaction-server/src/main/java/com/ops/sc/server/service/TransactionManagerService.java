package com.ops.sc.server.service;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.core.build.RpcResponseBuilder;
import com.ops.sc.rpc.grpc.TransactionManagerGrpc;
import com.ops.sc.server.service.impl.BranchTransStatusProcessorImpl;
import com.ops.sc.server.service.impl.TransProcessorImpl;
import com.ops.sc.core.util.ApplicationUtils;
import com.ops.sc.rpc.dto.*;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransactionManagerService extends TransactionManagerGrpc.TransactionManagerImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerService.class);

    private static BranchTransStatusProcessorImpl resourceStatusProcessor = ApplicationUtils
            .getBean(BranchTransStatusProcessorImpl.class);
    private static TransProcessorImpl globalTransProcessorImpl = ApplicationUtils
            .getBean(TransProcessorImpl.class);
    private static TransactionStatusService transactionStatusService = ApplicationUtils.getBean(TransactionStatusService.class);

    @Override
    public void startGlobalTrans(GlobalTransRequest registerGlobalTransRequest,
                                    StreamObserver<GlobalTransResponse> responseObserver) {

        LOGGER.info("received a GlobalTransRequest:{}",registerGlobalTransRequest);
        globalTransProcessorImpl.startGlobalTrans(registerGlobalTransRequest,responseObserver);

    }

    @Override
    public void startSagaGlobalTrans(GlobalSagaTransRequest request, StreamObserver<GlobalSagaTransResponse> responseObserver) {
        LOGGER.info("received a GlobalSagaTransRequest:{}",request);
        globalTransProcessorImpl.startGlobalTrans(request,responseObserver);
    }

    @Override
    public void rollbackGlobalTrans(GlobalTransRollbackRequest request, StreamObserver<GlobalTransRollbackResponse> responseObserver) {
        //super.rollbackGlobalTrans(request, responseObserver);
    }

    @Override
    public void findGlobalTrans(TransQueryRequest request, StreamObserver<TransQueryResponse> responseObserver) {
        CallBackResult<TransQueryResponse> callBackResult = execute(request);
        if (callBackResult.isSuccess) {
            responseObserver.onNext(callBackResult.response);
        } else {
            responseObserver
                    .onNext(TransQueryResponse.newBuilder().setBaseResponse(callBackResult.parentResponse).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void startBranchTrans(BranchTransRequest registerBranchTransRequest,
                                    StreamObserver<BranchTransResponse> responseObserver) {

        execute(registerBranchTransRequest);

        /*if (callBackResult.isSuccess()) {
            responseObserver.onNext(callBackResult.getResponse());
        } else {
            responseObserver
                    .onNext(BranchTransResponse.newBuilder().setBaseResponse(callBackResult.getParentResponse()).build());
        }
        responseObserver.onCompleted();*/
    }


    @Override
    public void regTransMsg(RegTransMsgRequest request, StreamObserver<RegTransMsgResponse> responseObserver) {
        execute(request);

        /*if (callBackResult.isSuccess()) {
            responseObserver.onNext(callBackResult.getResponse());
        } else {
            responseObserver
                    .onNext(RegTransMsgResponse.newBuilder().setBaseResponse(callBackResult.getParentResponse()).build());
        }
        responseObserver.onCompleted();*/
    }

    @Override
    public void executeBranchTrans(BranchTransRequest request, StreamObserver<BranchTransResponse> responseObserver) {
        CallBackResult<BranchTransResponse> callBackResult = execute(request);
        if (callBackResult.isSuccess) {
            responseObserver.onNext(callBackResult.response);
        } else {
            responseObserver
                    .onNext(BranchTransResponse.newBuilder().setBaseResponse(callBackResult.parentResponse).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void registerProducer(MQProducerRegRequest request,
            StreamObserver<MQProducerRegResponse> responseObserver) {
        execute(request);

        /*if (callBackResult.isSuccess()) {
            responseObserver.onNext(callBackResult.getResponse());
        } else {
            responseObserver
                    .onNext(MQProducerRegResponse.newBuilder().setBaseResponse(callBackResult.getParentResponse()).build());
        }
        responseObserver.onCompleted();*/
    }

    @Override
    public void stateCheck(StateServiceRequest request, StreamObserver<StateServiceResponse> responseObserver) {
        execute(request);

    }

    @Override
    public void compensateTrans(TransCompensationRequest request, StreamObserver<TransCompensationResponse> responseObserver) {
        CallBackResult<TransCompensationResponse> callBackResult = execute(request);
        if (callBackResult.isSuccess) {
            responseObserver.onNext(callBackResult.response);
        } else {
            responseObserver
                    .onNext(TransCompensationResponse.newBuilder().setBaseResponse(callBackResult.parentResponse)
                            .setBusinessId(request.getBusinessId()).setTid(request.getTid()).build());
        }
        responseObserver.onCompleted();
    }

    private <T> CallBackResult<T> execute(Object request) {
        ParentResponse parentResponse=null;
        T response = null;
        try {
            if (request instanceof BranchTransRequest) {
                BranchTransRequest branchTransRequest = (BranchTransRequest) request;
                if(branchTransRequest.getOperateType().getValue() == Constants.REGISTER){
                    response = (T) resourceStatusProcessor.registerBranch((BranchTransRequest) request);
                }
                else{
                    response = (T) resourceStatusProcessor.executeBranch((BranchTransRequest) request);
                }
            } else if (request instanceof GlobalTransRequest) {
                LOGGER.info("received a GlobalTransRequest:{}",request);
                //response = (T) globalTransProcessorImpl.startGlobalTrans((GlobalTransRequest) request,responseObserver);
            } else if (request instanceof RegTransMsgRequest) {
                response = (T) transactionStatusService.prepareTransMsg((RegTransMsgRequest) request);
            } else if (request instanceof MQProducerRegRequest) {
                response = (T) transactionStatusService.registerProducer((MQProducerRegRequest) request);
            } else if (request instanceof StateServiceRequest) {
                response = (T) transactionStatusService.stateCheck((StateServiceRequest) request);
            }else if(request instanceof TransCompensationRequest){
                response = (T) transactionStatusService.transactionCompensate((TransCompensationRequest) request);
            }else if(request instanceof TransQueryRequest){
                response = (T) transactionStatusService.findGlobalTrans((TransQueryRequest) request);
            }
            else {
                throw new IllegalArgumentException("can not handle request");
            }
        } catch (Exception e) {
            LOGGER.error("execute rpc callback error.", e);
            parentResponse = RpcResponseBuilder.buildErrorBaseResponse(TransactionResponseCode.INTERNAL_ERROR,null,e.getMessage());
            return new CallBackResult<>(parentResponse, response, false);
        }
        return new CallBackResult<>(parentResponse, response, true);
    }


    private class CallBackResult<T> {
        private ParentResponse parentResponse;
        private T response;
        private boolean isSuccess;

        private CallBackResult(ParentResponse parentResponse, T response, boolean isSuccess) {
            this.parentResponse = parentResponse;
            this.response = response;
            this.isSuccess = isSuccess;
        }
    }

}
