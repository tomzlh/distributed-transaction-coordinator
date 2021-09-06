package com.ops.sc.tc.mgr;


import com.ops.sc.common.trans.CommonTransInfo;
import com.ops.sc.common.trans.TransCommonResponse;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.rpc.dto.*;

public abstract class GlobalTransProcessBase {

    public abstract GlobalTransResponse startGlobal(final GlobalTransRequest request) throws RpcException;

    public abstract GlobalSagaTransResponse startSagaGlobal(final GlobalSagaTransRequest request) throws RpcException;

    public abstract GlobalTransRollbackResponse rollbackSagaGlobal(final GlobalTransRollbackRequest request) throws RpcException;


    public final BranchTransResponse commitBranchTrans(BranchTransRequest request,Long timeout) throws RpcException {
        beforeGlobalCommit();
        BranchTransResponse result = commit(request,timeout);
        afterGlobalCommit();
        return result;
    }

    public final BranchTransResponse rollBackBranchTrans(BranchTransRequest request,Long timeout) throws RpcException{
        beforeGlobalRollBack();
        BranchTransResponse result = rollback(request,timeout);
        afterGlobalRollBack();
        return result;
    }

    protected void beforeGlobalCommit() {}

    protected void afterGlobalCommit() {}

    protected void beforeGlobalRollBack() {}

    protected void afterGlobalRollBack() {}

    protected abstract BranchTransResponse commit(BranchTransRequest request, Long timeout) throws RpcException;

    protected abstract BranchTransResponse rollback(BranchTransRequest request, Long timeout) throws RpcException;

    protected abstract TransQueryResponse queryGlobalTrans(final TransQueryRequest transQueryRequest) throws RpcException;



}
