package com.ops.sc.ta.trans.xa;


import com.google.protobuf.UInt32Value;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.exception.ScClientException;

import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.common.context.XAContext;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.GlobalTransRequest;
import com.ops.sc.ta.buid.BranchTransBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;

import static com.ops.sc.common.constant.Constants.SC_GLOBAL_TRANS_PARAM;
import static com.ops.sc.common.constant.Constants.SC_LOGICAL_BRANCH_REGISTER_PARAM;


public class XADataSource extends AbstractDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(XADataSource.class);

    private boolean isDefault = false;
    private javax.sql.XADataSource targetXADataSource;

    public XADataSource() {
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public javax.sql.XADataSource getTargetXADataSource() {
        return targetXADataSource;
    }

    public void setTargetXADataSource(javax.sql.XADataSource targetXADataSource) {
        this.targetXADataSource = targetXADataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (!TransactionContextRecorder.isInTransaction()) {
            javax.sql.XAConnection xaConnection = targetXADataSource.getXAConnection();
            Connection connection = xaConnection.getConnection();
            return new XAConnection(connection, xaConnection);
        }
        javax.sql.XAConnection xaConnection = targetXADataSource.getXAConnection();
        return registerBranchAndStartXA(xaConnection);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        if (!TransactionContextRecorder.isInTransaction()) {
            javax.sql.XAConnection xaConnection = targetXADataSource.getXAConnection();
            Connection connection = xaConnection.getConnection();
            return new XAConnection(connection, xaConnection);
        }

        javax.sql.XAConnection xaConnection = targetXADataSource.getXAConnection(username, password);
        return registerBranchAndStartXA(xaConnection);
    }

    private XAConnection registerBranchAndStartXA(javax.sql.XAConnection xaConnection) throws SQLException {
        String branchId = "";
        boolean isClose = true;

        Connection connection = null;
        try {
            connection = xaConnection.getConnection();
            XAResource xaResource = xaConnection.getXAResource();
            //branchId = registerXABranch();
            //XATid XATid = new XATid(TransactionContextRecorder.getTid(), branchId);
           // xaResource.start(XATid, XAResource.TMNOFLAGS);
            XAContext xaContext = new XAContext(connection, xaResource, xaConnection,
                    TransactionContextRecorder.getTid(), branchId);

            TransactionContextRecorder.addXAContext(xaContext);
            isClose = false;
            return new XAConnection(xaContext);
        } catch (Exception e) {
            LOGGER.error("Start xa transaction fail, tid: {}, branchId: {} ", TransactionContextRecorder.getTid(),
                    branchId);
            throw new SQLException("Start xa transaction fail", e);
        } finally {
            if (isClose && xaConnection != null) {
                xaConnection.close();
            }
            if (isClose && connection != null) {
                connection.close();
            }
        }
    }

    private String registerXABranch() throws ScClientException {
        BranchTransRequest registerBranchRequest = (BranchTransRequest) TransactionContextRecorder
                .getRegisterContext().get().get(SC_LOGICAL_BRANCH_REGISTER_PARAM);
        if (registerBranchRequest == null) {
            if (TransactionContextRecorder.isInitiatorFromAspect()) {
                // 发起者本地业务
                GlobalTransRequest registerGlobalTransRequest = (GlobalTransRequest) TransactionContextRecorder
                        .getRegisterContext().get().get(SC_GLOBAL_TRANS_PARAM);
                if (registerGlobalTransRequest == null) {
                    throw new ScClientException(ClientErrorCode.INTERNAL_ERROR,
                            "Branch register params assemble failed!");
                }
                registerBranchRequest = BranchTransBeanBuilder
                        .globalParams2XABranchParams(registerGlobalTransRequest);

            } else if (TransactionContextRecorder.isParticipant()) {
                throw new ScClientException(ClientErrorCode.INTERNAL_ERROR,
                        "Branch register params assemble failed!");
            } else {
                throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED,
                        "Branch service not in @XATransaction annotation!");
            }
        }
        BranchTransRequest.Builder requestBuilder = registerBranchRequest.toBuilder();
        requestBuilder.setBranchType(UInt32Value.of(TransMode.XA.getValue()));
        //requestBuilder.setDataSource(XADataSourceRecorder.getBeanNameByXADataSource(this));
        /*BranchTransResponse response = TransActuatorRpcClientInit.getInstance()
                .getRMClient(TransactionContextRecorder.getServerAddress())
                .registerBranchTransSync(requestBuilder.build(), RpcConstants.REQUEST_TIMEOUT_MILLS);

        if (!ServerReponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED,
                    "Begin new xa branch fail:" + response.getBaseResponse().getMessage());
        }*/
        return null;
    }

    /**
     * 获取原生xaConnection
     *
     * @return
     * @throws SQLException
     */
    public javax.sql.XAConnection getOriginXAConnection() throws SQLException {
        return targetXADataSource.getXAConnection();
    }

}
