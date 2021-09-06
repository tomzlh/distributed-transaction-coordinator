package com.ops.sc.server.transaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.trans.CommonTransBranchInfo;
import com.ops.sc.common.trans.TransCommonResponse;
import com.ops.sc.common.trans.BaseTwoPhaseTransaction;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.constant.MessageConstants;
import com.ops.sc.core.config.ProducerConfigMap;
import com.ops.sc.common.exception.ScMessageException;
import com.ops.sc.core.config.TransMessageConfig;
import com.ops.sc.core.model.Message;
import com.ops.sc.core.model.RequestResult;
import com.ops.sc.core.trans.TransMessagePluginLoader;
import com.ops.sc.server.dao.TransMsgDaoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ops.sc.common.model.CommonTransMessage;


public abstract class TransMsgOperation implements BaseTwoPhaseTransaction<CommonTransBranchInfo> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransMsgOperation.class);

    public static TransMsgOperation newInstance(int type, ProducerConfigMap producerConfigMap) {
        Class clazz = TransMessagePluginLoader.getProducerClass(type);
        try {
            @SuppressWarnings("unchecked")
            Constructor constructor = clazz.getDeclaredConstructor(ProducerConfigMap.class);
            constructor.setAccessible(true);
            ClassLoader currentContextCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
            TransMsgOperation instance = (TransMsgOperation) constructor.newInstance(producerConfigMap);
            Thread.currentThread().setContextClassLoader(currentContextCL);
            return instance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new ScMessageException(ClientErrorCode.INTERNAL_ERROR, e);
        }
    }

    /**
     * 发送消息接口
     *
     * @param message
     * @return
     */
    public abstract RequestResult sendThrough(Message message);

    public abstract void close();

    /**
     * 事务准备
     *
     * @return
     */
    @Override
    public TransCommonResponse saveTransInfo(CommonTransBranchInfo base2PCInfo) {
        CommonTransMessage commonTransMessage = (CommonTransMessage) base2PCInfo;
        if (this instanceof NativeTransMsgOperation) {
            String payload = commonTransMessage.getPayload();
            Map<String, Object> traitMap = JsonUtil.toMap(commonTransMessage.getMetaData());

            RequestResult result = sendThrough(new Message(payload, traitMap));

            if (!result.isSuccess()) {
                throw new ScMessageException(ClientErrorCode.MSG_SEND_FAILED,
                        "Send message error" + result.getErrorMsg());
            }
            commonTransMessage.setPayload((String) result.getAttachment(MessageConstants.TOKEN));
        }
        TransMsgDaoFactory.getInstance().save(commonTransMessage);
        return TransCommonResponse.builder().build().success();
    }

    /**
     * 事务提交
     *
     * @param base2PCInfo
     * @return
     */
    @Override
    public TransCommonResponse commit(CommonTransBranchInfo base2PCInfo) {
        long tid = base2PCInfo.getTid();
        Long branchId = base2PCInfo.getBid();
        CommonTransMessage commonTransMessage = TransMsgDaoFactory.getInstance().findScMessage(tid, branchId);
        if (commonTransMessage == null) {
            LOGGER.info("Not found local message with tid :{} branchId : {}.", tid, branchId);
            return TransCommonResponse.builder().build().success();
        }
        if (TransMessageConfig.isRunInServerMode()) {
            if (commonTransMessage.getStatus().equals(TransStatus.COMMIT_SUCCEED.getValue())) {
                return TransCommonResponse.builder().build().success();
            }
            commonTransMessage.setStatus(TransStatus.COMMITTING.getValue());
            TransMsgDaoFactory.getInstance().updateStatusByTidBranchId(commonTransMessage);
        }
        RequestResult requestResult;
        if (this instanceof NativeTransMsgOperation) {
            NativeTransMsgOperation originTransactionMessageQueue = (NativeTransMsgOperation) this;
            String token = commonTransMessage.getPayload();
            if (!originTransactionMessageQueue.nativeCommit(token)) {
                return TransCommonResponse.builder().build().failed();
            }
            requestResult = RequestResult.ok();
        } else {
            String payload = commonTransMessage.getPayload();
            String metaData = commonTransMessage.getMetaData();
            Map<String, Object> traits = JsonUtil.toMap(metaData);
            requestResult = sendThrough(new Message(payload, traits));
        }

        if (requestResult.isSuccess()) {
            if (TransMessageConfig.isRunInServerMode()) {
                commonTransMessage.setStatus(TransStatus.COMMIT_SUCCEED.getValue());
                TransMsgDaoFactory.getInstance().updateStatusByTidBranchId(commonTransMessage);
            } else {
                TransMsgDaoFactory.getInstance().delete(tid, branchId);
            }
            LOGGER.debug("Send local message success, message : {} ", commonTransMessage.toString());
            return TransCommonResponse.builder().build().success();
        }
        LOGGER.warn("Send local message failed, message: {} ", commonTransMessage);
        return TransCommonResponse.builder().build().failed();
    }

    /**
     * 事务回滚
     *
     * @param base2PCInfo
     * @return
     */
    @Override
    public TransCommonResponse rollback(CommonTransBranchInfo base2PCInfo) {
        long tid = base2PCInfo.getTid();
        Long branchId = base2PCInfo.getBid();
        CommonTransMessage commonTransMessage = TransMsgDaoFactory.getInstance().findScMessage(tid, branchId);
        if (commonTransMessage == null) {
            LOGGER.info("tid : {}, branchId : {} not found", tid, branchId);
            return TransCommonResponse.builder().build().success();
        }
        if (TransMessageConfig.isRunInServerMode()) {
            if (commonTransMessage.getStatus().equals(TransStatus.CANCEL_SUCCEED.getValue())) {
                return TransCommonResponse.builder().build().success();
            }
            commonTransMessage.setStatus(TransStatus.CANCELLING.getValue());
            TransMsgDaoFactory.getInstance().updateStatusByTidBranchId(commonTransMessage);
        }
        if (this instanceof NativeTransMsgOperation) {
            NativeTransMsgOperation originTransactionMessageQueue = (NativeTransMsgOperation) this;
            String token = commonTransMessage.getPayload();
            if (!originTransactionMessageQueue.nativeRollback(token)) {
                return TransCommonResponse.builder().build().failed();
            }
        }
        if (TransMessageConfig.isRunInServerMode()) {
            commonTransMessage.setStatus(TransStatus.CANCEL_SUCCEED.getValue());
            TransMsgDaoFactory.getInstance().updateStatusByTidBranchId(commonTransMessage);
        } else {
            TransMsgDaoFactory.getInstance().delete(tid, branchId);
        }
        LOGGER.info("Rollback success, message : {}.", commonTransMessage);
        return TransCommonResponse.builder().build().success();
    }

}
