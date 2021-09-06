package com.ops.sc.server.service;


import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.BranchTransResponse;

public interface BranchTransStatusProcessor {

     BranchTransResponse registerBranch(final BranchTransRequest request);


     BranchTransResponse executeBranch(final BranchTransRequest request);


}
