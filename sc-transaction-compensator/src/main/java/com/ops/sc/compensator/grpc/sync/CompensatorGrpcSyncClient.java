package com.ops.sc.compensator.grpc.sync;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.rpc.dto.*;
import com.ops.sc.rpc.grpc.TransactionManagerGrpc;
import com.ops.sc.rpc.grpc.callback.CallBackServiceGrpc;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;
import com.ops.sc.compensator.grpc.GrpcClient;
import com.ops.sc.compensator.grpc.GrpcInitParams;
import io.grpc.Channel;
import io.grpc.Context;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;



public class CompensatorGrpcSyncClient extends GrpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompensatorGrpcSyncClient.class);

    private static final int DEFAULT_SLEEP_MAX_MILLIS = 2 * 60 * 1000;
    private static final int DEFAULT_SLEEP_MILLIS = 100;
    private static final int MAX_RETRY_COUNT = 20;
    private int sleepTime = DEFAULT_SLEEP_MILLIS;
    private int retryCount = 0;


    public static ExecutorService tcExecutor;

    public CompensatorGrpcSyncClient() {
        LOGGER.debug("Create new grpc client");
    }

    @Override
    public boolean shutdown() throws InterruptedException {
        tcExecutor.shutdown();
        return super.shutdown();
    }

    public void init(String serverAddress, GrpcInitParams config) {
        LOGGER.debug("Init grpc client, server address : {}, channel count : {}", serverAddress,
                config.getMaxChannelCount());
        this.serverAddress = serverAddress;
        super.createChannelFactory(serverAddress, config);
        tcExecutor = Executors.newFixedThreadPool(config.getThreadPoolSize());
    }



    public BranchTransResponse prepareBranchTransSync(final BranchTransRequest request, Long timeoutMS)
            throws RpcException {
        return syncCall(request, timeoutMS);
    }

    public BranchTransResponse commitBranchTransSync(final BranchTransRequest request,Long timeoutMS) throws RpcException{
        return syncCall(request, timeoutMS);
    }

    public BranchTransResponse rollbackBranchTransSync(final BranchTransRequest request,Long timeoutMS) throws RpcException{
        return syncCall(request, timeoutMS);
    }

    public BranchTransResponse commitSagaBranchTransSync(final BranchTransRequest request,Long timeoutMS) throws RpcException{
        return syncCall(request, timeoutMS);
    }

    public BranchTransResponse rollbackSagaBranchTransSync(final BranchTransRequest request,Long timeoutMS) throws RpcException{
        return syncCall(request, timeoutMS);
    }

    public TransCompensationResponse compensateTrans(final TransCompensationRequest request,Long timeoutMS) throws RpcException{
        return syncCall(request, timeoutMS);
    }



    private <T> T syncCall(Object request, Long timeoutMills) throws RpcException {
        try {
            Channel channel = channelFactory.getChannel();
            TransactionManagerGrpc.TransactionManagerBlockingStub stub = TransactionManagerGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(timeoutMills, TimeUnit.MILLISECONDS);
            if (request instanceof GlobalTransRollbackRequest) {
                return (T) stub.rollbackGlobalTrans((GlobalTransRollbackRequest) request);
            }else if (request instanceof StateServiceRequest) {
                return (T) stub.stateCheck((StateServiceRequest) request);
            }else if(request instanceof TransQueryRequest){
                return (T) stub.findGlobalTrans((TransQueryRequest) request);
            }else if(request instanceof BranchTransRequest){
                return (T) stub.executeBranchTrans((BranchTransRequest) request);
            }
            else if(request instanceof TransCompensationRequest){
                return (T) stub.compensateTrans((TransCompensationRequest) request);
            }
            else {
                throw new IllegalArgumentException("Not support this request type");
            }
        } catch (StatusRuntimeException e) {
            LOGGER.error("Request to server {} error: {}", serverAddress, e.getMessage());
            throw new RpcException(e.getStatus().asException());
        }
    }


    public String getServerAddress() {
        return serverAddress;
    }



}
