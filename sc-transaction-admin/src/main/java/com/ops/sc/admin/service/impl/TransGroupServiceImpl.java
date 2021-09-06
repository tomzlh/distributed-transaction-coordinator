package com.ops.sc.admin.service.impl;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import com.ops.sc.admin.dao.TransBranchInfoDao;
import com.ops.sc.admin.dao.TransInfoDao;
import com.ops.sc.common.bean.Page;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.bean.TransGroupQueryParams;
import com.ops.sc.common.bean.TransInfoQueryParams;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.utils.UUIDGenerator;
import com.ops.sc.common.exception.ScServerException;
import com.ops.sc.common.dto.admin.CreateTransGroupResult;
import com.ops.sc.common.dto.admin.TransGroupInfoDTO;
import com.ops.sc.common.dto.admin.TransGroupInfosResult;
import com.ops.sc.admin.service.TransGroupService;
import com.ops.sc.common.constant.ServerConstants;
import com.ops.sc.admin.dao.TransGroupDao;
import com.ops.sc.common.model.TransGroup;
import com.ops.sc.core.gather.TransInfoBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TransGroupServiceImpl implements TransGroupService {

    @Resource
    private TransGroupDao transGroupDao;

    @Resource
    private TransInfoDao transInfoDao;

    @Resource
    private TransBranchInfoDao transBranchInfoDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransGroupServiceImpl.class);

    @Override
    public CreateTransGroupResult createTransGroup(String tenantId, String groupName) {
        if (groupName.length() > ServerConstants.RequestParamsLimit.GROUP_NAME_MAX_LENGTH) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "GroupName is invalid");
        }
        if (transGroupDao.getTransGroupByTenantIdAndGroupName(tenantId, groupName) != null) {
            throw new ScServerException(TransactionResponseCode.PARAM_INVALID, "GroupName  is invalid");
        }

        String groupId = UUIDGenerator.generateUUID();
        TransGroup transGroup = new TransGroup();
        transGroup.setGroupId(groupId);
        transGroup.setGroupName(groupName);
        transGroup.setTenantId(tenantId);
        transGroupDao.save(transGroup);
        return new CreateTransGroupResult(groupId);
    }

    @Override
    public ResponseResult deleteTransGroup(String tenantId, String groupId) {
        TransGroup transGroup = transGroupDao.getTransGroupByGroupId(groupId);
        if (transGroup == null || !transGroup.getTenantId().equals(tenantId)) {
            throw new ScServerException(TransactionResponseCode.GROUP_NOT_EXIST,"group is not exist!");
        }
        transGroupDao.invalidGroup(groupId);
        transGroupDao.delete(groupId);
        cleanGroupData(groupId);
        return ResponseResult.returnSuccess();

    }

    private void cleanGroupData(String groupId) {
        LOGGER.info("Start to delete group transaction info, groupId : {} ", groupId);
        TransInfoQueryParams queryParams = new TransInfoQueryParams();
        queryParams.setGroupIdList(Arrays.asList(groupId));
        List<ScTransRecord> scTransRecords = transInfoDao.findByConditions(queryParams);
        scTransRecords.forEach(scTransRecord -> cleanTransData(scTransRecord.getTid()));
        LOGGER.info("End to delete transaction info, groupId : {} ", groupId);
    }

    @Transactional
    public void cleanTransData(Long tid) {
        transBranchInfoDao.delete(tid);
        transInfoDao.delete(tid);
    }

    @Override
    public TransGroupInfosResult getTransGroups(String tenantId, Integer limit, Integer offset,
                                                Boolean showAll) {

        int total = transGroupDao.getTransGroupCountByTenantId(tenantId);
        if (total == 0) {
            return new TransGroupInfosResult(total, new ArrayList<>());
        }
        TransGroupQueryParams transGroupQueryParams = new TransGroupQueryParams();
        transGroupQueryParams.setTenantId(tenantId);
        if (!showAll) {
            transGroupQueryParams.setPage(new Page(limit, offset));
        }
        List<TransGroup> transGroupList = transGroupDao.findByConditions(transGroupQueryParams);

        List<TransGroupInfoDTO> transGroupInfoList = transGroupList.stream().map(
                transGroup -> new TransGroupInfoDTO(transGroup.getGroupId(), transGroup.getGroupName(), transGroup.getCreateTime()))
                .collect(Collectors.toList());
        return new TransGroupInfosResult(total, transGroupInfoList);
    }

    @Override
    public Map<String, String> getGroupId2NameMap(String tenantId, String groupName) {
        Map<String, String> groupId2Name = new HashMap<>();

        // 获取groupIdList
        if (!StringUtils.isBlank(groupName)) {
            TransGroup transGroup = transGroupDao.getTransGroupByTenantIdAndGroupName(tenantId, groupName);
            if (transGroup != null) {
                groupId2Name.put(transGroup.getGroupId(), transGroup.getGroupName());
            }
        } else {
            List<TransGroup> transGroupList = transGroupDao.getTransGroupByTenantId(tenantId);
            transGroupList.forEach(transGroup -> groupId2Name.put(transGroup.getGroupId(), transGroup.getGroupName()));
        }
        return groupId2Name;
    }

    @Override
    public Map<String, String> getGroupId2NameMap(String tenantId) {
        return getGroupId2NameMap(tenantId, null);
    }

    private Integer getActiveAndFailedTransCount(String groupId) {
        List<Integer> statusList = new ArrayList<>(TransInfoBuilder.getNormalTransStatusList());
        statusList.addAll(TransInfoBuilder.getFailedTransStatusList());
        TransInfoQueryParams transInfoQueryParams = new TransInfoQueryParams();
        transInfoQueryParams.setGroupIdList(Collections.singletonList(groupId));
        transInfoQueryParams.setStatusList(statusList);
        return transInfoDao.getTotalCountByConditions(transInfoQueryParams);
    }


}
