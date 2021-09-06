package com.ops.sc.ta.clone.rollback.impl;

import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.core.clone.RollbackInfo;
import com.ops.sc.core.clone.RollbackItem;
import com.ops.sc.ta.clone.rollback.CallService;
import com.ops.sc.ta.clone.rollback.ImageNotConsistentException;
import com.ops.sc.ta.clone.rollback.ImageRecoverFactory;
import com.ops.sc.ta.dao.LogDao;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.ta.trans.datasource.ScDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;


@Service("callService")
public class CallServiceImpl implements CallService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallServiceImpl.class);

    @Resource(name = "imageRecoverFactory")
    private ImageRecoverFactory imageRecoverFactory;

    @Resource(name = "scLogDao")
    private LogDao logDao;

    @Override
    public boolean rollback(Long tid, Long branchId, ScDataSource scDataSource)
            throws SQLException, ImageNotConsistentException {
        Connection connection = null;
        try {
            // start transaction
            connection = scDataSource.getOriginalConnection();
            connection.setAutoCommit(false);

            ScTransRecord scTransRecord = logDao.findByTidAndBranchId(tid, branchId, connection);
            if (scTransRecord == null) {
                LOGGER.info("log not exist when rollback, tid : {}, branchId : {} ", tid, branchId);
                return true;
            }

            RollbackInfo rollbackInfo = JsonUtil.toObject(RollbackInfo.class, scTransRecord.getRollBackInfo());
            Collections.reverse(rollbackInfo.getInfo());
            for (RollbackItem item : rollbackInfo.getInfo()) {
                boolean canRollback = imageRecoverFactory.getRecover(item.getSqlType()).checkAfterImage(item,
                        connection);
                if (canRollback) {
                    imageRecoverFactory.getRecover(item.getSqlType()).rollbackBeforeImage(item, connection);

                } else {
                    LOGGER.error("Image not consistent, tid: {} , branchId : {} ", tid, branchId);
                    throw new ImageNotConsistentException(
                            "tid: " + tid + "branchId: " + branchId + "image not consistent");
                }
            }
            logDao.delete(tid, branchId, connection);
            LOGGER.debug("Rollback success, tid : {}, branchId : {} ", tid, branchId);
            connection.commit();
            return true;
        } catch (Exception e) {
            LOGGER.error("Rollback fail, tid: {}, branchId : {} ", tid, branchId, e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    LOGGER.error("Fail to rollback connection: ", rollbackException);
                }
            }
            if (e instanceof ImageNotConsistentException) {
                throw (ImageNotConsistentException) e;
            }

            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("Close connection fail: ", e);
                }
            }
        }
    }

    @Override
    public boolean commit(Long tid, Long branchId, ScDataSource scDataSource) throws SQLException {
        try (Connection connection = scDataSource.getOriginalConnection()) {
            ScTransRecord scTransRecord = logDao.findByTidAndBranchId(tid, branchId, connection);
            if (scTransRecord == null) {
                LOGGER.info("log not exist when commit, tid : {}, branchId : {}", tid, branchId);
                return true;
            }
            logDao.delete(tid, branchId, connection);
            LOGGER.debug("Commit success, tid : {}, branchId : {} ", tid, branchId);
            return true;
        }
    }
}
