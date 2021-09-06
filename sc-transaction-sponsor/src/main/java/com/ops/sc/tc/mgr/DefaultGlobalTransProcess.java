package com.ops.sc.tc.mgr;

import com.ops.sc.common.constant.RpcConstants;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.rpc.dto.*;
import com.ops.sc.tc.grpc.sync.SponsorGrpcSyncClientBoot;

public class DefaultGlobalTransProcess extends GlobalTransProcessBase {

    private DefaultGlobalTransProcess() {
    }

    public static DefaultGlobalTransProcess getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public GlobalTransResponse startGlobal(final GlobalTransRequest request) throws RpcException {
        long timeout = request.getTimeout()==null?RpcConstants.REQUEST_TIMEOUT_MILLS:request.getTimeout().getValue();
        return SponsorGrpcSyncClientBoot.getInstance().getTSClient(TransactionContextRecorder.getServerAddress())
                .startGlobalTransSync(request, timeout);
    }

    @Override
    public GlobalSagaTransResponse startSagaGlobal(GlobalSagaTransRequest request) throws RpcException {
        long timeout = request.getTimeout()==null?RpcConstants.REQUEST_TIMEOUT_MILLS:request.getTimeout().getValue();
        return SponsorGrpcSyncClientBoot.getInstance().getTSClient(TransactionContextRecorder.getServerAddress())
                .startGlobalSagaTransSync(request,timeout);
    }

    @Override
    public GlobalTransRollbackResponse rollbackSagaGlobal(GlobalTransRollbackRequest request) throws RpcException {
        long timeout = request.getTimeout()==null?RpcConstants.REQUEST_TIMEOUT_MILLS:request.getTimeout().getValue();
        return SponsorGrpcSyncClientBoot.getInstance().getTSClient(TransactionContextRecorder.getServerAddress())
                .rollbackGlobalTransSync(request,timeout);
    }



    public TransQueryResponse queryGlobalTrans(final TransQueryRequest transQueryRequest) throws RpcException{
        return SponsorGrpcSyncClientBoot.getInstance().getTSClient(TransactionContextRecorder.getServerAddress()).findGlobalTransSync(transQueryRequest, RpcConstants.REQUEST_TIMEOUT_MILLS);
    }


    @Override
    protected BranchTransResponse commit(BranchTransRequest request,Long timeout) throws RpcException{
        long rpcTimeout = timeout==null?RpcConstants.REQUEST_TIMEOUT_MILLS:timeout;
        return SponsorGrpcSyncClientBoot.getInstance().getTSClient(TransactionContextRecorder.getServerAddress())
                .commitBranchTransSync(request,rpcTimeout);
    }

    @Override
    protected BranchTransResponse rollback(BranchTransRequest request,Long timeout) throws RpcException{
        long rpcTimeout = timeout==null?RpcConstants.REQUEST_TIMEOUT_MILLS:timeout;
        return SponsorGrpcSyncClientBoot.getInstance().getTSClient(TransactionContextRecorder.getServerAddress())
                .rollbackBranchTransSync(request,rpcTimeout);
    }

    private static class SingletonHolder {
        public static final DefaultGlobalTransProcess INSTANCE = new DefaultGlobalTransProcess();
    }
}
