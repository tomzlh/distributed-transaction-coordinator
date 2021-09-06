package com.ops.sc.compensator.service.impl;

import com.ops.sc.common.exception.RpcException;
import com.ops.sc.compensator.grpc.sync.CompensatorGrpcSyncClient;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.BranchTransResponse;
import com.ops.sc.compensator.service.TransactionProcessor;
import com.ops.sc.rpc.dto.TransCompensationRequest;
import com.ops.sc.rpc.dto.TransCompensationResponse;
import org.springframework.stereotype.Service;

@Service
public class TransactionProcessorImpl implements TransactionProcessor {

    private final Long timeout=3000L;

    @Override
    public TransCompensationResponse compensate(TransCompensationRequest request, CompensatorGrpcSyncClient compensatorGrpcSyncClient) throws RpcException {
        return compensatorGrpcSyncClient.compensateTrans(request,timeout);
    }

    @Override
    public BranchTransResponse prepare(BranchTransRequest request, CompensatorGrpcSyncClient compensatorGrpcSyncClient) throws RpcException {
        BranchTransResponse branchTransResponse=compensatorGrpcSyncClient.prepareBranchTransSync(request,timeout);
        return branchTransResponse;
    }

    @Override
    public BranchTransResponse commit(BranchTransRequest request,CompensatorGrpcSyncClient compensatorGrpcSyncClient) throws RpcException {
        BranchTransResponse branchTransResponse=compensatorGrpcSyncClient.commitBranchTransSync(request,timeout);
        return branchTransResponse;
    }

    @Override
    public BranchTransResponse rollback(BranchTransRequest request,CompensatorGrpcSyncClient compensatorGrpcSyncClient) throws RpcException {
        BranchTransResponse branchTransResponse=compensatorGrpcSyncClient.rollbackBranchTransSync(request,timeout);
        return branchTransResponse;
    }
}
