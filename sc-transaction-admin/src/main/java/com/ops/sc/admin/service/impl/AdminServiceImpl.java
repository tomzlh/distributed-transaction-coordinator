package com.ops.sc.admin.service.impl;

import com.ops.sc.admin.dao.TransBranchInfoDao;
import com.ops.sc.admin.dao.TransInfoDao;
import com.ops.sc.admin.service.AdminService;
import com.ops.sc.admin.service.AlarmService;
import com.ops.sc.common.bean.*;
import com.ops.sc.common.enums.*;
import com.ops.sc.common.exception.ScServerException;
import com.ops.sc.common.dto.admin.FailedTransInfoDTO;
import com.ops.sc.common.dto.admin.FailedTransInfoListResult;
import com.ops.sc.common.dto.admin.AlarmEventResultList;
import com.ops.sc.common.dto.admin.BranchInfoDTO;
import com.ops.sc.common.dto.admin.BranchInfosResult;
import com.ops.sc.common.dto.admin.TransInfoDTO;
import com.ops.sc.common.dto.admin.TransInfosResult;
import com.ops.sc.common.dto.admin.TransSummaryResult;
import com.ops.sc.common.dto.admin.TransRecordResult;
import com.google.common.collect.Lists;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.core.gather.TransBranchInfoBuilder;
import com.ops.sc.core.gather.TransInfoBuilder;
import com.ops.sc.admin.service.TransGroupService;
import com.ops.sc.common.utils.DateUtil;
import com.ops.sc.admin.dao.TransGroupDao;
import com.ops.sc.common.model.TransGroup;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Resource
    private TransGroupDao transGroupDao;

    @Resource
    private TransInfoDao transInfoDao;

    @Resource
    private TransBranchInfoDao transBranchInfoDao;

    @Resource
    private TransGroupService transGroupService;

    @Resource
    private AlarmService alarmService;

    @Override
    public TransSummaryResult getTransSummary(String tenantId) {
        List<TransGroup> transGroupList = transGroupDao.getTransGroupByTenantId(tenantId);
        if (CollectionUtils.isEmpty(transGroupList)) {
            return new TransSummaryResult();
        }
        List<String> groupIdList = transGroupList.stream().map(TransGroup::getGroupId).collect(Collectors.toList());
        List<Integer> activeStatusList = TransInfoBuilder.getNormalTransStatusList();
        List<Integer> failedStatusList = TransInfoBuilder.getFailedTransStatusList();
        int normalTccTransNum = transInfoDao.getTransCountByGroupAndStatus(groupIdList, activeStatusList);
        int normalBranchTccTransNum = transBranchInfoDao.getBranchTransCountByGroupAndGlobalTransStatus(groupIdList,
                activeStatusList);
        int failedBranchTccTransNum = transBranchInfoDao.getBranchTransCountByGroupAndStatus(groupIdList, failedStatusList);
        int failedTransNum = transInfoDao.getTransCountByGroupAndStatus(groupIdList, failedStatusList);
        return new TransSummaryResult(normalTccTransNum, normalBranchTccTransNum, failedBranchTccTransNum, failedTransNum);
    }



    @Override
    public ResponseResult cleanTransResource(String groupId) {
        LOGGER.info("Clean transaction Resource with GroupId: {}",groupId);
        transGroupDao.delete(groupId);
        return ResponseResult.returnSuccess();
    }

    @Override
    public TransInfosResult getTccInfos(String tenantId,
                                        TransInfoRequestQueryParams queryParams) {
        Map<String, String> groupId2Name;
        if (queryParams.getTid()==null) {
            groupId2Name = transGroupService.getGroupId2NameMap(tenantId,queryParams.getTransGroupName());
        } else {
            groupId2Name = transGroupService.getGroupId2NameMap(tenantId);
        }
        List<String> groupIdList = new ArrayList<>(groupId2Name.keySet());
        if (CollectionUtils.isEmpty(groupIdList)) {
            return new TransInfosResult(0, new ArrayList<>());
        }

        TransInfoQueryParams transInfoQueryParams;
        try {
            transInfoQueryParams = TransInfoBuilder.buildTccTransInfoQueryParams(groupIdList, queryParams);
        } catch (IllegalArgumentException e) {
            return new TransInfosResult(0, new ArrayList<>());
        }
        int total = transInfoDao.getTotalCountByConditions(transInfoQueryParams);
        List<ScTransRecord> transactionInfoList = transInfoDao.findByConditions(transInfoQueryParams);
        List<TransInfoDTO> transInfoDTOList = transactionInfoList.stream()
                .map(transInfo -> TransInfoBuilder.buildTransDTO(transInfo))
                .collect(Collectors.toList());
        return new TransInfosResult(total, transInfoDTOList);
    }

    @Override
    public BranchInfosResult getBranchInfos(String tenantId, Long tid) {
        ScTransRecord transactionInfo = transInfoDao.findByTid(tid);
        if (transactionInfo == null) {
            throw new ScServerException(TransactionResponseCode.TRANS_NOT_EXIST,"Transaction is not exist!");
        }
        TransGroup transGroup = transGroupDao.getTransGroupByGroupId(transactionInfo.getGroupId());
        if (transGroup == null || !tenantId.equals(transGroup.getTenantId())) {
            throw new ScServerException(TransactionResponseCode.TRANS_NOT_EXIST,"Transaction is not exist!");
        }
        List<ScBranchRecord> transBranchInfoList = transBranchInfoDao.findByTid(tid);
        List<BranchInfoDTO> branchInfoDTOList = transBranchInfoList.stream()
                .filter(transBranchInfo -> !TransProcessMode.LOGIC_BRANCH.getValue().equals(transBranchInfo.getTransMode()))
                .map(TransBranchInfoBuilder::getTransBranchInfo).collect(Collectors.toList());
        return new BranchInfosResult(branchInfoDTOList);
    }


    @Override
    public AlarmEventResultList getAlarmEventInfos(String filterEventName) {
        if (StringUtils.isBlank(filterEventName)) {
            return alarmService.getAllAlarmEventInfos();
        }
        AlarmEvent alarmEvent;
        try {
            alarmEvent = AlarmEvent.getAlarmEventByEventName(filterEventName);
        } catch (IllegalArgumentException e) {
            return new AlarmEventResultList(new ArrayList<>());
        }
        return alarmService.getAlarmEventInfo(alarmEvent);
    }

    @Override
    public FailedTransInfoListResult getFailedTccInfos(FailureTransInfoRequestParams params) {
        Map<String, String> groupId2Name = transGroupService.getGroupId2NameMap(params.getTenantId(),
               params.getTransGroupName());
        List<String> groupIdList = new ArrayList<>(groupId2Name.keySet());
        if (CollectionUtils.isEmpty(groupIdList)) {
            return new FailedTransInfoListResult();
        }
        List<Integer> failedStatus = TransInfoBuilder.getFailedTransStatusList();
        TransInfoQueryParams transInfoQueryParams = TransInfoBuilder.buildParams(groupIdList, failedStatus, params);
        int total = transInfoDao.getTotalCountByConditions(transInfoQueryParams);
        List<ScTransRecord> transactionInfoList = transInfoDao.findByConditions(transInfoQueryParams);
        List<FailedTransInfoDTO> failedTransInfoDTOList = Lists.newArrayList();
        transactionInfoList.forEach((transInfo -> {
            FailedTransInfoDTO dto = new FailedTransInfoDTO();
            dto.setCallerIp(transInfo.getCallerIp());
            dto.setTransGroupName(groupId2Name.get(transInfo.getGroupId()));
            dto.setCreateTime(DateUtil.date2String(transInfo.getCreateTime()));
            dto.setStatus(transInfo.getStatus());
            dto.setTid(transInfo.getTid());
            dto.setBizId(transInfo.getBusinessId());
            dto.setId(transInfo.getId());
            dto.setAppName(transInfo.getAppName());
            dto.setBizId(transInfo.getBusinessId());
            TransBranchInfoQueryParams branchInfoQueryParams = new TransBranchInfoQueryParams();
            branchInfoQueryParams.setTid(transInfo.getTid());
            branchInfoQueryParams.setStatusList(failedStatus);
            List<BranchInfoDTO> branchInfoDTOList = transBranchInfoDao.findByConditions(branchInfoQueryParams).stream()
                    .filter(transBranchInfo -> !TransProcessMode.LOGIC_BRANCH.getValue().equals(transBranchInfo.getTransMode()))
                    .map(TransBranchInfoBuilder::getTransBranchInfo).collect(Collectors.toList());
            dto.setFailedBranchTransList(branchInfoDTOList);
            failedTransInfoDTOList.add(dto);
        }));
        return new FailedTransInfoListResult(total, failedTransInfoDTOList);
    }

    /**
     * 事务查询
     *
     * @param tid
     * @return
     */
    @Override
    public TransRecordResult getTransRecord(Long tid) {
        ScTransRecord transactionInfo = transInfoDao.findByTid(tid);
        if (null == transactionInfo) {
            throw new ScServerException(TransactionResponseCode.TRANS_NOT_EXIST,"Transaction is not exist!");
        }
        TransRecordResult transRecordResult = new TransRecordResult();
        transRecordResult.setTid(tid);
        transRecordResult.setBusinessId(transactionInfo.getBusinessId());
        transRecordResult.setAppName(transactionInfo.getAppName());
        transRecordResult.setStartTime(DateUtil.date2String(transactionInfo.getCreateTime()));
        transRecordResult.setStatus(transactionInfo.getStatus());
        transRecordResult.setEndTime(
                transactionInfo.getEndTime() == null ? StringUtils.EMPTY : DateUtil.date2String(transactionInfo.getEndTime()));
        List<ScBranchRecord> scBranchRecordList=transBranchInfoDao.findByTid(tid);
        List<TransRecordResult.Children> childrenArrayList = Lists.newArrayList();
        if(scBranchRecordList!=null){
            for(ScBranchRecord scBranchRecord:scBranchRecordList){
                TransRecordResult.Children children=new TransRecordResult.Children();
                children.setAppName(scBranchRecord.getBranchTransName());
                children.setBranchId(scBranchRecord.getBid());
                children.setTid(scBranchRecord.getTid());
                children.setParentId(scBranchRecord.getParentName());
                children.setTransType(TransMode.fromId(scBranchRecord.getTransMode()).name());
                children.setStatus(TransStatus.getTransStatusByValue(scBranchRecord.getStatus()).name());
                children.setTransactionName(scBranchRecord.getTransactionName());
                childrenArrayList.add(children);
            }
        }
        return transRecordResult;
    }




}
