package com.ops.sc.ta.trans;

import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.rpc.dto.*;

public class DefaultTaCallOutService implements TaCallOut {

    private TaCallOut TaCallOut;

    private DefaultTaCallOutService() {

    }

    public static TaCallOut getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 分支注册
     *
     * @param request
     * @return
     */
    @Override
    public BranchTransResponse registerBranch(BranchTransRequest request) throws ScClientException {
        return TaCallOut.registerBranch(request);
    }


    @Override
    public BranchTransInfoList queryBranchInfoList(BranchTransQueryRequest branchTransQueryRequest) {
        return null;
    }

    private static class SingletonHolder {
        private static DefaultTaCallOutService instance = new DefaultTaCallOutService();
    }
}
