package com.ops.sc.core.gather;

import com.ops.sc.common.model.ExtensionBranchInfo;
import com.ops.sc.common.model.TransBranchInfo;
import com.ops.sc.common.model.TransMessage;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.model.CommonTransMessage;


public class TransMessageBuilder {

    public static TransMessage assembleTransMessage(CommonTransMessage commonTransMessage) {
        TransMessage transMessage = new TransMessage();
        transMessage.setProducerId(Long.valueOf(commonTransMessage.getProducerName()));
        transMessage.setBid(commonTransMessage.getBid());
        transMessage.setMetaData(commonTransMessage.getMetaData());
        transMessage.setPayload(commonTransMessage.getPayload());
        transMessage.setTid(commonTransMessage.getTid());
        return transMessage;
    }

    public static CommonTransMessage assembleGenericTransMessage(TransBranchInfo transBranchInfo, TransMessage transMessage) {
        CommonTransMessage commonTransMessage = new CommonTransMessage();
        commonTransMessage.setProducerName(transMessage.getProducerId().toString());
        commonTransMessage.setStatus(transBranchInfo.getStatus());
        commonTransMessage.setTid(transBranchInfo.getTid());
        commonTransMessage.setMetaData(transMessage.getMetaData());
        commonTransMessage.setPayload(transMessage.getPayload());
        commonTransMessage.setBid(transMessage.getBid());
        commonTransMessage.setParentId(transBranchInfo.getParentName());
        commonTransMessage.setClientLocalIp(transBranchInfo.getCallerIp());
        return commonTransMessage;
    }

    public static CommonTransMessage assembleGenericTransMessage(ScBranchRecord transBranchInfo, TransMessage transMessage) {
        CommonTransMessage commonTransMessage = new CommonTransMessage();
        commonTransMessage.setProducerName(transMessage.getProducerId().toString());
        commonTransMessage.setStatus(transBranchInfo.getStatus());
        commonTransMessage.setTid(transBranchInfo.getTid());
        commonTransMessage.setMetaData(transMessage.getMetaData());
        commonTransMessage.setPayload(transMessage.getPayload());
        commonTransMessage.setBid(transMessage.getBid());
        commonTransMessage.setParentId(transBranchInfo.getParentName());
        commonTransMessage.setClientLocalIp(transBranchInfo.getCallerIp());
        return commonTransMessage;
    }

    public static CommonTransMessage assembleGenericTransMessage(ExtensionBranchInfo enhancedTransBranchInfo) {
        CommonTransMessage commonTransMessage = new CommonTransMessage();
        commonTransMessage.setProducerName(enhancedTransBranchInfo.getProducerId());
        commonTransMessage.setStatus(enhancedTransBranchInfo.getStatus());
        commonTransMessage.setTid(enhancedTransBranchInfo.getTid());
        commonTransMessage.setMetaData(enhancedTransBranchInfo.getMetaData());
        commonTransMessage.setPayload(enhancedTransBranchInfo.getPayload());
        commonTransMessage.setBid(enhancedTransBranchInfo.getBid());
        commonTransMessage.setParentId(enhancedTransBranchInfo.getParentName());
        commonTransMessage.setClientLocalIp(enhancedTransBranchInfo.getCallerIp());
        commonTransMessage.setAppName(enhancedTransBranchInfo.getBranchTransName());
        return commonTransMessage;
    }

}
