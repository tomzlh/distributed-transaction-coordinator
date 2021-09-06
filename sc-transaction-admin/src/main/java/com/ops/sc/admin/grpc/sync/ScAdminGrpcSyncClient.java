package com.ops.sc.admin.grpc.sync;

import com.ops.sc.admin.grpc.GrpcClient;
import com.ops.sc.admin.grpc.GrpcInitParams;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.rpc.dto.*;
import com.ops.sc.rpc.grpc.TransactionManagerGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ScAdminGrpcSyncClient extends GrpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScAdminGrpcSyncClient.class);

    private static final int DEFAULT_SLEEP_MAX_MILLIS = 2 * 60 * 1000;
    private static final int DEFAULT_SLEEP_MILLIS = 100;
    private static final int MAX_RETRY_COUNT = 20;
    private int sleepTime = DEFAULT_SLEEP_MILLIS;
    private int retryCount = 0;


    public static ExecutorService tcExecutor;

    public ScAdminGrpcSyncClient() {
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




    private <T> T syncCall(Object request, Long timeoutMills) throws RpcException {
        try {
            Channel channel = channelFactory.getChannel();
            TransactionManagerGrpc.TransactionManagerBlockingStub stub = TransactionManagerGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(timeoutMills, TimeUnit.MILLISECONDS);
            if(request instanceof BranchTransRequest){
                return (T) stub.executeBranchTrans((BranchTransRequest) request);
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
