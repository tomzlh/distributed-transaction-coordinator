package com.ops.sc.ta.callback.impl;


import com.ops.sc.ta.callback.TransactionParticipant;
import com.ops.sc.ta.clone.rollback.CallService;
import com.ops.sc.ta.clone.rollback.ImageNotConsistentException;
import com.ops.sc.ta.trans.datasource.ScDataSource;
import com.ops.sc.ta.trans.support.ScDataSourceRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;


@Service("transactionParticipant")
public class TransactionParticipantImpl implements TransactionParticipant {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionParticipantImpl.class);

    @Resource(name = "callService")
    private CallService callService;

    @Override
    public Boolean commit(Long tid, Long branchId, String dataSource) throws SQLException {
        ScDataSource scDataSource = ScDataSourceRecorder.getDataSourceByBeanName(dataSource);
        if (scDataSource == null) {
            throw new NoSuchBeanDefinitionException(dataSource);
        }
        return callService.commit(tid, branchId, scDataSource);
    }

    @Override
    public Boolean rollback(Long tid, Long branchId, String dataSource)
            throws SQLException, ImageNotConsistentException {
        ScDataSource scDataSource = ScDataSourceRecorder.getDataSourceByBeanName(dataSource);
        if (scDataSource == null) {
            throw new NoSuchBeanDefinitionException(dataSource);
        }

        return callService.rollback(tid, branchId, scDataSource);

    }

}
