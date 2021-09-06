package com.ops.sc.admin.controller;

import javax.annotation.Resource;

import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.constant.ServerConstants;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.exception.ScServerException;
import com.ops.sc.admin.service.TransGroupService;
import com.ops.sc.common.dto.admin.CreateTransGroupResult;
import com.ops.sc.common.dto.admin.TransGroupInfosResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/dtc")
public class TransGroupController{
    @Resource
    private TransGroupService transGroupService;

    @GetMapping(params = "Action=" + ServerConstants.HttpAction.CREATE_TRANS_GROUP)
    public CreateTransGroupResult createTransGroup(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId,
                                                   @RequestParam(value = "GroupName") String groupName) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }

        if (StringUtils.isBlank(groupName)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "GroupName is empty!");
        }

        return transGroupService.createTransGroup(tenantId, groupName);
    }

    @GetMapping(params = "Action=" + ServerConstants.HttpAction.DELETE_TRANS_GROUP)
    public ResponseResult deleteTransGroup(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId,
                                           @RequestParam(value = "GroupId") String groupId) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }

        if (StringUtils.isBlank(groupId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, "TransGroupId");
        }

        return transGroupService.deleteTransGroup(tenantId, groupId);
    }

    @GetMapping(params = "Action=" + ServerConstants.HttpAction.GET_TRANS_GROUPS)
    public TransGroupInfosResult getTransGroups(@RequestHeader(value = ServerConstants.HttpConst.HEADER_TENANTID) String tenantId,
                                                @RequestParam(value = "Limit", defaultValue = ServerConstants.HttpConst.DEFAULT_LIMIT, required = false) Integer limit,
                                                @RequestParam(value = "Offset", defaultValue = ServerConstants.HttpConst.DEFAULT_OFFSET, required = false) Integer offset,
                                                @RequestParam(value = "ShowAll", defaultValue = "false", required = false) Boolean showAll) {

        if (StringUtils.isBlank(tenantId)) {
            throw new ScServerException(TransactionResponseCode.PARAM_IS_REQUIRED, ServerConstants.HttpConst.HEADER_TENANTID);
        }

        if (limit < 0) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "Limit");
        }

        if (offset < 0) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "Offset");
        }
        return transGroupService.getTransGroups(tenantId, limit, offset, showAll);
    }
}
