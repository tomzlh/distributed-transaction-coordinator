package com.ops.sc.admin.service;

import java.util.Map;

import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.dto.admin.CreateTransGroupResult;
import com.ops.sc.common.dto.admin.TransGroupInfosResult;


public interface TransGroupService {
    /**
     *
     * @param tenantId
     * @param groupName
     * @return
     */
    CreateTransGroupResult createTransGroup(String tenantId, String groupName);

    /**
     *
     * @param tenantId
     * @param groupId
     * @return
     */
    ResponseResult deleteTransGroup(String tenantId, String groupId);

    /**
     *
     * @param tenantId
     * @param limit
     * @param offset
     * @return
     */
    TransGroupInfosResult getTransGroups(String tenantId, Integer limit, Integer offset, Boolean showAll);


    Map<String, String> getGroupId2NameMap(String tenantId,  String groupName);

    Map<String, String> getGroupId2NameMap(String tenantId);

}
