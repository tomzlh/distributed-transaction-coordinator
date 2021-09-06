package com.ops.sc.ta.trans;


import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.rpc.dto.*;

public interface TaCallOut {

    /**
     * 分支注册
     * @param request
     * @return
     */
    BranchTransResponse registerBranch(final BranchTransRequest request) throws ScClientException;



    BranchTransInfoList queryBranchInfoList(BranchTransQueryRequest branchTransQueryRequest);

}
