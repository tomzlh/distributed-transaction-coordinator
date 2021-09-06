package com.ops.sc.server.service.impl;

import com.ops.sc.common.trans.CommonTransBranchInfo;
import com.ops.sc.server.transaction.ClientTransMsgQueueRegister;
import com.ops.sc.server.transaction.TransMsgOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ops.sc.common.trans.TransactionExecutor;


@Component("scMessageParticipant")
public class TransactionExecutorImpl implements TransactionExecutor {

    private static Logger logger = LoggerFactory.getLogger(TransactionExecutorImpl.class);

    @Override
    public boolean commit(Long tid, Long branchId, String extraData) {
        TransMsgOperation transMsgOperation = ClientTransMsgQueueRegister.getInstance()
                .getTransactionMessageQueue(extraData);
        if (transMsgOperation == null) {
            logger.info("ProducerName: {} not load", extraData);
            return false;
        }
        return transMsgOperation.commit(new CommonTransBranchInfo(tid, branchId)).isSuccess();
    }

    @Override
    public boolean rollback(Long tid, Long branchId, String extraData) {
        TransMsgOperation transMsgOperation = ClientTransMsgQueueRegister.getInstance()
                .getTransactionMessageQueue(extraData);
        if (transMsgOperation == null) {
            logger.info("ProducerName: {} not load", extraData);
            return false;
        }
        return transMsgOperation.rollback(new CommonTransBranchInfo(tid, branchId)).isSuccess();
    }
}
