package com.ops.sc.ta.util;


import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.bean.MessageInfo;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.common.model.CommonTransMessage;


public class ScTransMessageBuilder {

    public static ScTransRecord transferToLog(CommonTransMessage commonTransMessage) throws ScClientException {
        ScTransRecord globalTransLog = new ScTransRecord();
        globalTransLog.setTid(commonTransMessage.getTid());
        globalTransLog.setBusinessId(commonTransMessage.getBusinessId());
        globalTransLog.setStatus(commonTransMessage.getStatus());
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMetaData(commonTransMessage.getMetaData());
        messageInfo.setPayload(commonTransMessage.getPayload());
        messageInfo.setProducerName(commonTransMessage.getProducerName());
        globalTransLog.setRollBackInfo(JsonUtil.toString(messageInfo));

        return globalTransLog;
    }

    public static CommonTransMessage transferFromLog(ScTransRecord globalTransLog) {
        CommonTransMessage commonTransMessage = new CommonTransMessage();
        commonTransMessage.setStatus(globalTransLog.getStatus());
        commonTransMessage.setTid(globalTransLog.getTid());
        commonTransMessage.setBusinessId(globalTransLog.getBusinessId());
        MessageInfo messageInfo = JsonUtil.toObject(MessageInfo.class, globalTransLog.getRollBackInfo());
        if (messageInfo != null) {
            commonTransMessage.setProducerName(messageInfo.getProducerName());
            commonTransMessage.setMetaData(messageInfo.getMetaData());
            commonTransMessage.setPayload(messageInfo.getPayload());
        }
        return commonTransMessage;
    }

}
