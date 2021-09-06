package com.ops.sc.ta.handler;

import com.ops.sc.common.bean.*;
import com.ops.sc.common.enums.CallErrorCode;
import com.ops.sc.common.enums.MessageType;
import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.ta.executor.TccClientExecutor;
import com.ops.sc.ta.executor.TransClientExecutorFactory;
import com.ops.sc.ta.executor.XaClientExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransactionHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionHandlerFactory.class);


    public static TransactionHandlerFactory getInstance() {
        return TransactionHandlerFactory.SingletonHolder.instance;
    }

    public ScResponseMessage handler(ScRequestMessage scRequestMessage){
        ScResponseMessage responseMessage = null;
        try {
            TransMode transMode =TransMode.valueOf(scRequestMessage.getTransMode());
            switch (transMode){
                case TCC:
                    TccClientExecutor tccClientHandler = TransClientExecutorFactory.getTccExecutor(scRequestMessage.getTransactionName());
                    MessageType messageType=MessageType.getByValue(scRequestMessage.getMessageType());
                    switch (messageType){
                        case TYPE_BRANCH_PREPARE:
                            responseMessage=tccClientHandler.prepare(scRequestMessage);
                            break;
                        case TYPE_BRANCH_COMMIT:
                            responseMessage=tccClientHandler.confirm(scRequestMessage);
                            break;
                        case TYPE_BRANCH_ROLLBACK:
                            responseMessage=tccClientHandler.cancel(scRequestMessage);
                            break;
                        default:
                            LOGGER.warn("not support message type:{}", scRequestMessage);
                    }
                    break;
                case XA:
                    XaClientExecutor xaClientHandler = TransClientExecutorFactory.getXaExecutor(scRequestMessage.getTransactionName());
                    messageType=MessageType.getByValue(scRequestMessage.getMessageType());
                    switch (messageType){
                        case TYPE_BRANCH_PREPARE:
                            responseMessage=xaClientHandler.prepare(scRequestMessage);
                            break;
                        case TYPE_BRANCH_COMMIT:
                            responseMessage=xaClientHandler.confirm(scRequestMessage);
                            break;
                        case TYPE_BRANCH_ROLLBACK:
                            responseMessage=xaClientHandler.cancel(scRequestMessage);
                            break;
                        default:
                            LOGGER.warn("not support message type:{}", scRequestMessage);
                    }
                    break;
            }
        }catch (ScClientException e){
            LOGGER.error(CallErrorCode.NetDispatch.getErrorCode(), e);
            responseMessage = makeParentResponseMessage(scRequestMessage, e.getClientErrorCode().getErrorCode(),e.getMessage());
        }
        catch (Throwable th) {
            LOGGER.error(CallErrorCode.NetDispatch.getErrorCode(), th);
            responseMessage = makeParentResponseMessage(scRequestMessage, TransactionResponseCode.TRANSACTION_PROCESS_FAILED.getCode(), th.getMessage());
        }
        return responseMessage;
    }

    private ScResponseMessage makeParentResponseMessage(ScRequestMessage scRequestMessage, String errorCode, String message) {
        ScResponseMessage responseMessage;
        responseMessage = new ScResponseMessage();
        responseMessage.setMessageType(scRequestMessage.getMessageType());
        responseMessage.setBranchId(scRequestMessage.getBranchId());
        responseMessage.setBusinessId(scRequestMessage.getBusinessId());
        responseMessage.setMsg(message);
        responseMessage.setTid(scRequestMessage.getTid());
        ScResponseMessage.ResultInfo resultInfo=new ScResponseMessage.ResultInfo();
        resultInfo.code= errorCode;
        resultInfo.message= message;
        responseMessage.setResultInfo(resultInfo);
        return responseMessage;
    }

    private static class SingletonHolder {
        private static TransactionHandlerFactory instance = new TransactionHandlerFactory();
    }

}
