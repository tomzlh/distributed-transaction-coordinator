package com.ops.sc.ta.trans;


import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;

import java.sql.SQLException;


public interface TaCallIn {


    RpcCallBackRequest prepare(final RpcCallBackResponse rpcCallBackResponse) throws ScClientException,SQLException;
    /**
     * 分支提交
     *
     * @param rpcCallBackResponse
     * @return
     */
    RpcCallBackRequest commitBranch(final RpcCallBackResponse rpcCallBackResponse) throws ScClientException,SQLException;

    /**
     * 分支回滚
     *
     * @param rpcCallBackResponse
     */
    RpcCallBackRequest rollBackBranch(final RpcCallBackResponse rpcCallBackResponse) throws ScClientException,SQLException;
}
