package com.ops.sc.ta.callback.impl;

import com.ops.sc.common.enums.GlobalTransStatus;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.ta.callback.TransactionInitiator;
import com.ops.sc.ta.dao.LogDao;
import com.ops.sc.ta.trans.datasource.ScDataSource;
import com.ops.sc.ta.trans.support.ScDataSourceRecorder;
import com.ops.sc.ta.trans.xa.XADataSource;
import com.ops.sc.ta.trans.xa.XADataSourceRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.XAConnection;
import java.sql.Connection;
import java.sql.SQLException;


@Service("transactionInitiator")
public class TransactionInitiatorImpl implements TransactionInitiator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionInitiatorImpl.class);

    @Resource(name = "scLogDao")
    private LogDao logDao;


    @Override
    public GlobalTransStatus checkBack(Long tid, String dataSource) throws SQLException {
        ScDataSource scDataSource = ScDataSourceRecorder.getDataSourceByBeanName(dataSource);
        XADataSource xaDataSource = XADataSourceRecorder.getXADataSourceByBeanName(dataSource);
        if (scDataSource == null && xaDataSource == null) {
            throw new NoSuchBeanDefinitionException(dataSource);
        }
        LOGGER.debug("Start to check back, tid : {}", tid);

        if (scDataSource != null) {
            return checkBack(tid, scDataSource);
        } else {
            return checkBack(tid, xaDataSource);
        }
    }

    private GlobalTransStatus checkBack(Long tid, ScDataSource scDataSource) throws SQLException {
        try (Connection connection = scDataSource.getOriginalConnection()) {
            ScTransRecord scTransRecord = logDao.findInitiatorByTid(tid, connection);
            return handleScLog(scTransRecord);
        }
    }

    private GlobalTransStatus checkBack(Long tid, XADataSource xaDataSource) throws SQLException {
        XAConnection xaConnection = null;
        Connection connection = null;
        try {
            xaConnection = xaDataSource.getOriginXAConnection();
            connection = xaConnection.getConnection();
            ScTransRecord scTransRecord = logDao.findInitiatorByTid(tid, connection);
            return handleScLog(scTransRecord);
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (xaConnection != null) {
                xaConnection.close();
            }
        }
    }

    private GlobalTransStatus handleScLog(ScTransRecord scTransRecord) {
        if (scTransRecord == null) {
            return GlobalTransStatus.NOT_EXIST;
        } else if (TransStatus.TRY_SUCCEED.getValue().equals(scTransRecord.getStatus())) {
            return GlobalTransStatus.SUCCESS;
        } else if (TransStatus.TRY_FAILED.getValue().equals(scTransRecord.getStatus())) {
            return GlobalTransStatus.FAILED;
        } else {
            return GlobalTransStatus.TRYING;
        }
    }
}
