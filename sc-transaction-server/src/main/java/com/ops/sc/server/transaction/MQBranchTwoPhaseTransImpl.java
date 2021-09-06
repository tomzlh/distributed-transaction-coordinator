package com.ops.sc.server.transaction;

import com.ops.sc.common.model.TransMQProducer;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.trans.TransCommonResponse;
import com.ops.sc.server.service.TransMessageQueueService;
import com.ops.sc.core.service.TransMessageService;
import com.ops.sc.common.model.ExtensionBranchInfo;
import com.ops.sc.common.model.TransMessage;
import com.ops.sc.core.gather.TransMessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service("mqBranchTwoPhaseTransaction")
public class MQBranchTwoPhaseTransImpl extends BranchTwoPhaseTransImpl {

    @Resource
    private TransMessageService transMessageService;

    @Resource
    private TransMessageQueueService serverTransMQRegister;

    /**
     * 事务准备
     *
     * @param transBranchInfo
     * @return
     */
    @Override
    public TransCommonResponse saveTransInfo(ScBranchRecord transBranchInfo) {
        ExtensionBranchInfo enhancedBranchInfo = (ExtensionBranchInfo) transBranchInfo;
        TransMQProducer transMqProducer = transMessageService.getProducerByProducerId(enhancedBranchInfo.getProducerId());
        TransMsgOperation transMsgOperation = getOrCreateTransactionMessageQueue(transMqProducer);
        return transMsgOperation.saveTransInfo(TransMessageBuilder.assembleGenericTransMessage(enhancedBranchInfo));
    }

    /**
     * 事务提交
     *
     * @param transBranchInfo
     * @return
     */
    @Override
    public TransCommonResponse commit(ScBranchRecord transBranchInfo) {
        TransMessage transMessage = transMessageService.getByBranchId(transBranchInfo.getBid());
        TransMQProducer transMqProducer = transMessageService.getProducerByProducerId(String.valueOf(transMessage.getProducerId()));
        TransMsgOperation transMsgOperation = getOrCreateTransactionMessageQueue(transMqProducer);
        return transMsgOperation.commit(TransMessageBuilder.assembleGenericTransMessage(transBranchInfo, transMessage));
    }

    /**
     * 事务回滚
     *
     * @param transBranchInfo
     * @return
     */
    @Override
    public TransCommonResponse rollback(ScBranchRecord transBranchInfo) {
        TransMessage transMessage = transMessageService.getByBranchId(transBranchInfo.getBid());
        TransMQProducer transMqProducer = transMessageService.getProducerByProducerId(String.valueOf(transMessage.getProducerId()));
        TransMsgOperation transMsgOperation = getOrCreateTransactionMessageQueue(transMqProducer);
        return transMsgOperation.rollback(TransMessageBuilder.assembleGenericTransMessage(transBranchInfo, transMessage));
    }

    private TransMsgOperation getOrCreateTransactionMessageQueue(TransMQProducer transMqProducer) {
        String producerName = transMqProducer.getId().toString();
        Integer type = transMqProducer.getType();
        return serverTransMQRegister.getOrCreateTransactionMessageQueue(producerName, type, transMqProducer.getConfig());
    }

}
