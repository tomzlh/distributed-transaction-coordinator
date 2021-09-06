package com.ops.sc.ta.trans.xa;


import com.ops.sc.common.enums.CallBackType;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.ta.trans.TaCallIn;
import com.ops.sc.common.bean.XATid;
import com.ops.sc.rpc.grpc.callback.RpcCallBackRequest;
import com.ops.sc.rpc.grpc.callback.RpcCallBackResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;


@Service
public class XARMCallIn implements TaCallIn {
    private static final Logger LOGGER = LoggerFactory.getLogger(XARMCallIn.class);

    public RpcCallBackRequest handleCallBack(final RpcCallBackResponse rpcCallBackResponse) throws SQLException {
        if (CallBackType.COMMIT.getValue() == rpcCallBackResponse.getCallBackType()) {
            return this.commitBranch(rpcCallBackResponse);
        }
        if (CallBackType.ROLLBACK.getValue() == rpcCallBackResponse.getCallBackType()) {
            return this.rollBackBranch(rpcCallBackResponse);
        }

        throw new UnsupportedOperationException("Unknown callBackType!");
    }

    @Override
    public RpcCallBackRequest prepare(RpcCallBackResponse rpcCallBackResponse) throws ScClientException, SQLException {
        XADataSource xaDataSource = XADataSourceRecorder
                .getXADataSourceByBeanName(rpcCallBackResponse.getDataSource());
        if (xaDataSource == null) {
            throw new NoSuchBeanDefinitionException(rpcCallBackResponse.getDataSource());
        }
        XATid xaTid = new XATid(Long.parseLong(rpcCallBackResponse.getTid()), Long.parseLong(rpcCallBackResponse.getBranchId()));
        XATransactionManager.getInstance().prepareXA(xaTid, xaDataSource.getTargetXADataSource());
        LOGGER.debug("Prepare xa success, tid: {}, branchId: {}", xaTid.getTid(), xaTid.getBranchId());
        return RpcCallBackRequest.newBuilder().setTwoPCResult(true).setRequestId(rpcCallBackResponse.getRequestId())
                .build();
    }

    /**
     * 分支提交
     *
     * @param rpcCallBackResponse
     * @return
     */
    @Override
    public RpcCallBackRequest commitBranch(RpcCallBackResponse rpcCallBackResponse) throws SQLException {
        XADataSource xaDataSource = XADataSourceRecorder
                .getXADataSourceByBeanName(rpcCallBackResponse.getDataSource());
        if (xaDataSource == null) {
            throw new NoSuchBeanDefinitionException(rpcCallBackResponse.getDataSource());
        }
        XATid xaTid = new XATid(Long.parseLong(rpcCallBackResponse.getTid()), Long.parseLong(rpcCallBackResponse.getBranchId()));
        XATransactionManager.getInstance().commitXA(xaTid, xaDataSource.getTargetXADataSource());
        LOGGER.debug("Commit xa success, tid: {}, branchId: {}", xaTid.getTid(), xaTid.getBranchId());
        return RpcCallBackRequest.newBuilder().setTwoPCResult(true).setRequestId(rpcCallBackResponse.getRequestId())
                .build();
    }

    /**
     * 分支回滚
     *
     * @param rpcCallBackResponse
     */
    @Override
    public RpcCallBackRequest rollBackBranch(RpcCallBackResponse rpcCallBackResponse) throws SQLException {
        XADataSource xaDataSource = XADataSourceRecorder
                .getXADataSourceByBeanName(rpcCallBackResponse.getDataSource());
        if (xaDataSource == null) {
            throw new NoSuchBeanDefinitionException(rpcCallBackResponse.getDataSource());
        }
        XATid xaTid = new XATid(Long.parseLong(rpcCallBackResponse.getTid()), Long.parseLong(rpcCallBackResponse.getBranchId()));
        XATransactionManager.getInstance().rollbackXA(xaTid, xaDataSource.getTargetXADataSource());
        LOGGER.debug("Rollback xa success, tid: {}, branchId: {}", xaTid.getTid(), xaTid.getBranchId());
        return RpcCallBackRequest.newBuilder().setTwoPCResult(true).setRequestId(rpcCallBackResponse.getRequestId())
                .build();
    }
}
