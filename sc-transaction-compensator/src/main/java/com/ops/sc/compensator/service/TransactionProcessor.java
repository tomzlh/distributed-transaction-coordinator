package com.ops.sc.compensator.service;

import com.ops.sc.common.exception.RpcException;
import com.ops.sc.compensator.grpc.sync.CompensatorGrpcSyncClient;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.BranchTransResponse;
import com.ops.sc.rpc.dto.TransCompensationRequest;
import com.ops.sc.rpc.dto.TransCompensationResponse;

public interface TransactionProcessor {

     TransCompensationResponse compensate(TransCompensationRequest request, CompensatorGrpcSyncClient compensatorGrpcSyncClient) throws RpcException;

     BranchTransResponse prepare(BranchTransRequest request, CompensatorGrpcSyncClient compensatorGrpcSyncClient) throws RpcException;

     BranchTransResponse commit(BranchTransRequest request,CompensatorGrpcSyncClient compensatorGrpcSyncClient) throws RpcException;

     BranchTransResponse rollback(BranchTransRequest request,CompensatorGrpcSyncClient compensatorGrpcSyncClient) throws RpcException;

}
