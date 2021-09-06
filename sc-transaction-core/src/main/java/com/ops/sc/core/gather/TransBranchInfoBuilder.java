package com.ops.sc.core.gather;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.TimeoutType;
import com.ops.sc.common.enums.TransProcessMode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.dto.admin.BranchInfoDTO;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.utils.DateUtil;
import com.ops.sc.common.model.ExtensionBranchInfo;
import com.ops.sc.common.model.TransBranchInfo;
import com.ops.sc.common.model.CommonTransMessage;
import com.ops.sc.rpc.dto.RegTransMsgRequest;


public class TransBranchInfoBuilder {


    public static ExtensionBranchInfo getEnhancedTransBranchInfo(final RegTransMsgRequest request,
                                                                 Long bid) {
        ExtensionBranchInfo extensionBranchInfo = new ExtensionBranchInfo();
        extensionBranchInfo.setTid(Long.parseLong(request.getTid()));
        extensionBranchInfo.setBid(bid);
        extensionBranchInfo.setParentName(request.getParentId());
        extensionBranchInfo.setStatus(TransStatus.TRY_SUCCEED.getValue());
        extensionBranchInfo.setCallerIp(request.getCallerIp());
        extensionBranchInfo.setRetryCount(0);
        TransProcessMode branchType = request.getSupportNativeTransaction() ? TransProcessMode.MQ_NATIVE_REMOTE
                : TransProcessMode.MQ_REMOTE;
        //extensionBranchInfo.setTransType(branchType.getValue());
        extensionBranchInfo.setBranchName(request.getAppName());
        extensionBranchInfo.setTimeout(Constants.DEFAULT_TIMEOUT);
        extensionBranchInfo.setTimeoutType(TimeoutType.ALARM.getValue());
        extensionBranchInfo.setMetaData(request.getExtensionData());
        extensionBranchInfo.setPayload(request.getMsgBody());
        extensionBranchInfo.setProducerId(request.getProducerId());
        extensionBranchInfo.setBranchTransName(request.getAppName());
        return extensionBranchInfo;
    }

    public static TransBranchInfo getTransBranchInfo(CommonTransMessage commonTransMessage) {
        TransBranchInfo transBranchInfo = new TransBranchInfo();
        transBranchInfo.setTid(commonTransMessage.getTid());
        transBranchInfo.setBid(commonTransMessage.getBid());
        transBranchInfo.setParentName(commonTransMessage.getParentId());
        transBranchInfo.setStatus(TransStatus.TRY_SUCCEED.getValue());
        transBranchInfo.setCallerIp(commonTransMessage.getClientLocalIp());
        transBranchInfo.setRetryCount(0);
        transBranchInfo.setBranchName(commonTransMessage.getTransactionName());
        transBranchInfo.setTimeout(Constants.DEFAULT_TIMEOUT);
        transBranchInfo.setTimeoutType(TimeoutType.ALARM.getValue());
       // transBranchInfo.setUData(commonTransMessage.getProducerName().getBytes(SC_DEFAULT_CHARSET));
        transBranchInfo.setBranchTransName(commonTransMessage.getAppName());
        return transBranchInfo;
    }

    public static BranchInfoDTO getTransBranchInfo(ScBranchRecord transBranchInfo) {
        BranchInfoDTO branchInfoDTO = new BranchInfoDTO();
        branchInfoDTO.setBid(transBranchInfo.getBid());
        branchInfoDTO.setBranchName(transBranchInfo.getBranchName());
        branchInfoDTO.setCallerIp(transBranchInfo.getCallerIp());
        branchInfoDTO.setStatus(transBranchInfo.getStatus());
       // branchInfoDTO.setBranchType(transBranchInfo.getTransType());
        branchInfoDTO.setCreateTime(DateUtil.date2String(transBranchInfo.getCreateTime()));
        branchInfoDTO.setAppName(transBranchInfo.getBranchTransName());
        Integer retryCount = transBranchInfo.getRetryCount();
        branchInfoDTO.setRetryCount(retryCount != null && retryCount > 0 ? retryCount - 1 : 0);
        return branchInfoDTO;
    }

}
