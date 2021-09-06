package com.ops.sc.ta.trans.xa;

import com.ops.sc.common.bean.XATid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;

public class XATransactionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(XATransactionManager.class);

    private static XATransactionManager instance = new XATransactionManager();

    private XATransactionManager() {
    }

    public static XATransactionManager getInstance() {
        return instance;
    }

    /**
     * 一阶段提交xaConnection

     * @return
     */
    public boolean prepareXA(XATid XATid, javax.sql.XADataSource xaDataSource) throws SQLException{
        XAConnection xaConnection = null;
        try {
            xaConnection = xaDataSource.getXAConnection();
            XAResource xaResource = xaConnection.getXAResource();
            xaResource.end(XATid, XAResource.TMSUCCESS);
            int result = xaResource.prepare(XATid);
            LOGGER.debug("Prepare xa resource, tid: {} branch: {}  result: {}", XATid.getTid(),
                    XATid.getBranchId(), result);
            return result == XAResource.XA_RDONLY || result == XAResource.XA_OK;
        } catch (XAException e) {
            LOGGER.error("Prepare xa fail, tid: {} branch: {}  result: {} ", XATid.getTid(),
                    XATid.getBranchId(), e);
            return false;
        }
    }


    /*private boolean submitXAConnections(XATid XATid) {
        List<XAContext> xaContexts = TransactionContextRecorder.getCurrentXAContextInfo();
        boolean isPrepareSucceed = true;
        for (XAContext xaContext : xaContexts) {
            if (isPrepareSucceed) {
                prepareXA(XATid,xaContext);
            } else {
                rollbackOnePhaseXA(XATid,xaContext);
            }
        }
        return isPrepareSucceed;
    }*/



    public void commitXA(XATid XATid, javax.sql.XADataSource xaDataSource) throws SQLException {
        XAConnection xaConnection = null;
        try {
            xaConnection = xaDataSource.getXAConnection();
            XAResource xaResource = xaConnection.getXAResource();
            xaResource.commit(XATid, false);
        } catch (XAException e) {
            if (e.errorCode != XAException.XAER_NOTA) {
                LOGGER.error("Commit xa fail, tid: {}, branchId: {}, error code: {}, error msg: {}", XATid.getTid(),
                        XATid.getBranchId(), e.errorCode, e.getMessage());
                throw new SQLException("Commit xa fail", e);
            }
        } finally {
            if (xaConnection != null) {
                xaConnection.close();
            }
        }
    }

    public void rollbackXA(XATid XATid, XADataSource xaDataSource) throws SQLException {
        XAConnection xaConnection = null;
        try {
            xaConnection = xaDataSource.getXAConnection();
            XAResource xaResource = xaConnection.getXAResource();
            xaResource.rollback(XATid);
        } catch (XAException e) {
            if (e.errorCode != XAException.XAER_NOTA) {
                LOGGER.error("Rollback xa fail, tid: {}, branchId: {}, error code: {}, error msg: {}",
                        XATid.getTid(), XATid.getBranchId(), e.errorCode, e.getMessage());
                throw new SQLException("Rollback xa fail", e);
            }
        } finally {
            if (xaConnection != null) {
                xaConnection.close();
            }
        }
    }
}
