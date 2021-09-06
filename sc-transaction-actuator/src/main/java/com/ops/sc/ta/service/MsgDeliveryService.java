package com.ops.sc.ta.service;

import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.common.exception.ScMessageException;
import com.ops.sc.ta.trans.DefaultTaCallOutService;
import com.ops.sc.rpc.dto.*;
import com.ops.sc.core.model.Message;
import com.google.common.base.Strings;
import com.google.protobuf.UInt32Value;
import com.ops.sc.common.enums.TransProcessMode;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.utils.InetUtil;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.common.enums.ClientErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MsgDeliveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgDeliveryService.class);

    /**
     * 发送事务消息到server
     *
     * @param registerProducerId
     * @param message
     */
    public static void sendMessageToServer(AnnotationProcessService annotationProcessor, String registerProducerId,
                                           Message message, String resourceDisplayName, boolean supportNativeTransaction) {
        if (!TransactionContextRecorder.isInTransaction()) {
            throw new ScMessageException(ClientErrorCode.UNSUPPORTED, "Not in @ScTransaction scope");
        }
        if (Strings.isNullOrEmpty(message.getPayload())) {
            throw new ScMessageException(ClientErrorCode.MSG_CONFIG_ERROR, "Message payload can not be empty");
        }
        String metaData = JsonUtil.toString(message.getTraits());
        if (Strings.isNullOrEmpty(metaData)) {
            throw new ScMessageException(ClientErrorCode.MSG_CONFIG_ERROR, "Message attribute can not be empty");
        }
        Long tid = TransactionContextRecorder.getTid();
        Long parentId = TransactionContextRecorder.getParentId();
        RegTransMsgRequest.Builder builder = RegTransMsgRequest.newBuilder();
        builder.setTid(String.valueOf(tid));
        builder.setMsgBody(message.getPayload());
        builder.setExtensionData(metaData);
        builder.setParentId(String.valueOf(parentId));
        builder.setProducerId(registerProducerId);
        builder.setAppName(annotationProcessor.getAppName());
        builder.setCallerIp(InetUtil.getHostIp());
        builder.setSupportNativeTransaction(supportNativeTransaction);

        /*RegTransMsgResponse response;
        try {
            response = TransActuatorRpcClientInit.getInstance().getRMClient(TransactionContextRecorder.getServerAddress())
                    .regTransMsgSync(builder.build(), RpcConstants.REQUEST_TIMEOUT_MILLS);
        } catch (RpcException e) {
            LOGGER.error("Send message {} to server fail", builder.build());
            throw new ScMessageException(ClientErrorCode.MSG_SEND_FAILED, "Send message to server fail", e);
        }

        if (!ServerReponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
            LOGGER.error("Send message {} to server error : {}", builder.build(),
                    response.getBaseResponse().getMessage());
            throw new ScMessageException(ClientErrorCode.MSG_SEND_FAILED, "Send message to server error");
        } else {
            LOGGER.debug("Send message to server successfully branchId: {}", response.getBranchId());
        }*/
    }

    /**
     * 注册事物消息Producer
     *
     * @param type
     * @param config
     * @return
     */
    public static String registerMessageProducer(Integer type, String config) {
        MQProducerRegRequest.Builder builder = MQProducerRegRequest.newBuilder();

        builder.setType(UInt32Value.of(type));
        builder.setConfig(config);

        /*MQProducerRegResponse response;
        try {
            response = TransActuatorRpcClientInit.getInstance().getRMClient(TransactionContextRecorder.getServerAddress())
                    .producerRegisterSync(builder.build(), RpcConstants.REQUEST_TIMEOUT_MILLS);
        } catch (RpcException e) {
            throw new ScMessageException(ClientErrorCode.CLIENT_REQUEST_FAILED, "Register producer to server failed : ",
                    e);
        }
        if (!ServerReponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
            LOGGER.error("Register producer:{} to server failed:{}.", config, response);
            throw new ScMessageException(ClientErrorCode.CLIENT_REQUEST_FAILED, "Register producer to server failed.");
        } else {
            LOGGER.info("Register producer:{} - {} to server successfully!", config, response.getProducerId());
            return response.getProducerId();
        }*/
        return null;
    }

    /**
     * 注册分支事物
     *
     * @return
     */
    public static Long registerLocalMessageBranch(AnnotationProcessService annotationProcessor, String producerName,
                                                    String transactionName, boolean isNativeTransactionMQ) throws ScClientException {
        if (!TransactionContextRecorder.isInTransaction()) {
            throw new ScMessageException(ClientErrorCode.UNSUPPORTED, "Not In Transaction Scope");
        }
        Long tid = TransactionContextRecorder.getTid();
        Long parentId = TransactionContextRecorder.getParentId();
        BranchTransRequest.Builder requestBuilder = BranchTransRequest.newBuilder();
        requestBuilder.setTid(String.valueOf(tid));
        requestBuilder.setCallerIp(InetUtil.getHostIp());
        TransProcessMode branchType = isNativeTransactionMQ ? TransProcessMode.MQ_NATIVE_LOCAL
                : TransProcessMode.MQ_LOCAL;
        requestBuilder.setBranchType(UInt32Value.of(branchType.getValue()));
        requestBuilder.setBranchTransName(annotationProcessor.getAppName());
        BranchTransResponse response = DefaultTaCallOutService.getInstance()
                .registerBranch(requestBuilder.build());
        if (!TransactionResponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
            LOGGER.error("Register branch fail: {}", response.getBaseResponse().getMessage());
            throw new ScMessageException(ClientErrorCode.CLIENT_REQUEST_FAILED, "Register Branch Failed! ");
        }
        return Long.parseLong(response.getBranchId());
    }

}
