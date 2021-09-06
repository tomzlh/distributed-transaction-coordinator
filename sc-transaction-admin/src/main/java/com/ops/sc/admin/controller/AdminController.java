package com.ops.sc.admin.controller;

import javax.annotation.Resource;

import com.ops.sc.common.bean.FailureTransInfoRequestParams;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.constant.ServerConstants;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.exception.ScServerException;
import com.ops.sc.admin.service.AdminService;
import com.ops.sc.common.dto.admin.AlarmEventResultList;
import com.ops.sc.common.dto.admin.BranchInfosResult;
import com.ops.sc.common.dto.admin.TransInfosResult;
import com.ops.sc.common.dto.admin.TransSummaryResult;
import com.ops.sc.common.dto.admin.TransRecordResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ops.sc.common.bean.TransInfoRequestQueryParams;

import java.util.Date;

@RestController
@RequestMapping("/dtc")
public class AdminController {

    @Resource
    private AdminService adminService;

    @GetMapping(params = "Action=" + ServerConstants.HttpAction.GET_TRANS_SUMMARY)
    public TransSummaryResult getTransSummary(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }
        return adminService.getTransSummary(tenantId);
    }


    @GetMapping(params = "Action=" + ServerConstants.HttpAction.CLEAN_TRANS_RESOURCE)
    public ResponseResult cleanTransResource(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID, required = false) String tenantId) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }

        return adminService.cleanTransResource(tenantId);
    }


    @GetMapping(params = "Action=" + ServerConstants.HttpAction.GET_TCC_TRANS_INFOS)
    public TransInfosResult getTccTransInfos(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId,
                                             @RequestParam(value = "GroupName", required = false) String transGroupName,
                                             @RequestParam(value = "TransName", required = false) String transName,
                                             @RequestParam(value = "Status", required = false) Integer status,
                                             @RequestParam(value = "SearchStartTime", required = false) Long searchStartTime,
                                             @RequestParam(value = "SearchEndTime", required = false) Long searchEndTime,
                                             @RequestParam(value = "BizId", required = false) String bizId,
                                             @RequestParam(value = "AppName", required = false) String appName,
                                             @RequestParam(value = "Tid", required = false) Long tid,
                                             @RequestParam(value = "Limit", required = false, defaultValue = ServerConstants.HttpConst.DEFAULT_LIMIT) Integer limit,
                                             @RequestParam(value = "ActiveTrans", required = false, defaultValue = "false") Boolean activeTrans) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }

        if (searchStartTime != null && searchStartTime < 0) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "SearchStartTime");
        }

        if (searchEndTime != null && searchEndTime < 0) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "SearchEndTime");
        }

        if (searchStartTime != null && searchEndTime != null && searchEndTime < searchStartTime) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "SearchStartTime/SearchEndTime");
        }

        if (limit < 0) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "Limit");
        }

        TransInfoRequestQueryParams queryParams = getTransInfoRequestQueryParams(transGroupName, transName, status, searchStartTime, searchEndTime, bizId, appName, tid, limit, activeTrans);
        return adminService.getTccInfos(tenantId, queryParams);
    }

    private TransInfoRequestQueryParams getTransInfoRequestQueryParams(String transGroupName, String transName, Integer status, Long searchStartTime, Long searchEndTime, String bizId, String appName, Long tid, Integer limit,Boolean activeTrans) {
        TransInfoRequestQueryParams queryParams = new TransInfoRequestQueryParams(activeTrans, limit, 0);
        queryParams.setTransGroupName(transGroupName);
        queryParams.setTransName(transName);
        queryParams.setStatus(status);
        queryParams.setSearchTimeStart(new Date(searchStartTime));
        queryParams.setSearchTimeEnd(new Date(searchEndTime));
        queryParams.setBizId(bizId);
        queryParams.setTid(tid);
        queryParams.setActiveTrans(activeTrans);
        queryParams.setAppName(appName);
        return queryParams;
    }


    @GetMapping(params = "Action=" + ServerConstants.HttpAction.GET_FAILED_TCC_TRANS_INFOS)
    public ResponseResult getFailedTccTransInfos(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId,
                                                 @RequestParam(value = "AppName", required = false) String appName,
                                                 @RequestParam(value = "TransGroupName", required = false, defaultValue = StringUtils.EMPTY) String transGroupName,
                                                 @RequestParam(value = "SearchStartTime", required = false) Long searchStartTime,
                                                 @RequestParam(value = "SearchEndTime", required = false) Long searchEndTime,
                                                 @RequestParam(value = "Limit", required = false, defaultValue = ServerConstants.HttpConst.DEFAULT_LIMIT) Integer limit) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }

        if (searchStartTime != null && searchStartTime < 0) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "SearchStartTime");
        }

        if (searchEndTime != null && searchEndTime < 0) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "SearchEndTime");
        }

        if (searchStartTime != null && searchEndTime != null && searchEndTime < searchStartTime) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "SearchStartTime/SearchEndTime");
        }

        if (limit < 0) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "Limit");
        }

        FailureTransInfoRequestParams params = getFailureTransInfoRequestParams(tenantId, appName, transGroupName, searchStartTime, searchEndTime, limit);
        return adminService.getFailedTccInfos(params);
    }


    private FailureTransInfoRequestParams getFailureTransInfoRequestParams(String tenantId, String appName, String transGroupName, Long searchStartTime, Long searchEndTime, Integer limit) {
        FailureTransInfoRequestParams params = new FailureTransInfoRequestParams();
        params.setTenantId(tenantId);
        params.setSearchTimeEnd(new Date(searchEndTime));
        params.setSearchTimeStart(new Date(searchStartTime));
        params.setLimit(limit);
        params.setTransGroupName(transGroupName);
        params.setAppName(appName);
        return params;
    }

    @GetMapping(params = "Action=" + ServerConstants.HttpAction.GET_BRANCH_TRANS_INFOS)
    public BranchInfosResult getBranchTransInfos(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId,
                                                 @RequestParam(value = "Tid") Long tid) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }

        if (tid==null) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "Tid");
        }

        return adminService.getBranchInfos(tenantId, tid);
    }


    @GetMapping(params = "Action=" + ServerConstants.HttpAction.GET_ALARM_EVENT_INFOS)
    public AlarmEventResultList getAlarmEventInfos(
            @RequestParam(value = "AlarmEventName", required = false) String eventName) {
        return adminService.getAlarmEventInfos(eventName);
    }


    @GetMapping(params = "Action=" + ServerConstants.HttpAction.GET_TRANS_RECORD)
    public TransRecordResult getTransRecord(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId,
                                            @RequestParam(value = "Tid") Long tid) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }

        if (tid==null) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "Tid");
        }

        return adminService.getTransRecord(tid);
    }

}
