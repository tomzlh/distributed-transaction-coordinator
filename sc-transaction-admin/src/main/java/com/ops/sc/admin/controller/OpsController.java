package com.ops.sc.admin.controller;

import javax.annotation.Resource;

import com.ops.sc.common.constant.ServerConstants;
import com.ops.sc.common.exception.ScServerException;
import com.ops.sc.admin.service.OpsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;


@RestController
@RequestMapping("/dtc")
public class OpsController{

    @Resource
    private OpsService opsService;

    @GetMapping(params = "Action=" + ServerConstants.HttpAction.RETRY_BRANCH_TRANS)
    public ResponseResult retryBranchTrans(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId,
                                           @RequestParam(value = "Tid") Long tid,
                                           @RequestParam(value = "BranchId") Long branchId) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }

        if (tid==null) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "Tid");
        }

        if (branchId==null) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "BranchId");
        }

        return opsService.retryFailBranchTrans(tenantId, tid, branchId);
    }

    @GetMapping(params = "Action=" + ServerConstants.HttpAction.CANCEL_TIMEOUT_BRANCH_TRANS)
    public ResponseResult cancelTimeoutBranchTrans(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId,
                                                   @RequestParam(value = "Tid") Long tid,
                                                   @RequestParam(value = "BranchId") Long branchId) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }


        if (tid==null) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "Tid");
        }

        if (branchId==null) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "BranchId");
        }

        return opsService.cancelTimeoutBranchTrans(tenantId, tid, branchId);
    }

    @GetMapping(params = "Action=" + ServerConstants.HttpAction.CHECK_BACK_TIMEOUT_GLOBAL_TRANS)
    public ResponseResult checkBackTimeoutGlobalTrans(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId,
                                                      @RequestParam(value = "Tid") Long tid) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }


        if (tid==null) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "Tid");
        }

        return opsService.checkBackTimeoutGlobalTrans(tenantId, tid);
    }

    @GetMapping(params = "Action=" + ServerConstants.HttpAction.GET_GRPC_STREAM_MAP)
    public ResponseResult getGrpcStreamMap() {
        return opsService.getGrpcStreamMap();
    }

}
