package com.ops.sc.server.dao.impl;

import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.utils.DateUtil;
import com.ops.sc.common.bean.TransBranchInfoQueryParams;
import com.ops.sc.mybatis.mapper.TransBranchInfoMapper;
import com.ops.sc.server.dao.TransBranchInfoDao;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 分支事务操作类
 */
@Component
public class TransBranchInfoDaoImpl implements TransBranchInfoDao {

    @Resource
    private TransBranchInfoMapper transBranchInfoMapper;

    @Override
    public void delete(Long tid) {
        transBranchInfoMapper.delete(tid);
    }

    @Override
    public void save(ScBranchRecord transBranchInfo) {
        if (transBranchInfo.getStatus() == null) {
            transBranchInfo.setStatus(TransStatus.TRYING.getValue());
        }
        Date now = new Date();
        if (transBranchInfo.getCreateTime() == null) {
            transBranchInfo.setCreateTime(now);
        }
        if (transBranchInfo.getModifyTime() == null) {
            transBranchInfo.setModifyTime(now);
        }
        transBranchInfoMapper.save(transBranchInfo);
    }

    @Override
    public void save(List<ScBranchRecord> transBranchInfos) {
        transBranchInfoMapper.batchSave(transBranchInfos);
    }

    @Override
    public List<ScBranchRecord> findByConditions(TransBranchInfoQueryParams transBranchInfoQueryParams) {
        return transBranchInfoMapper.findByConditions(transBranchInfoQueryParams);
    }

    @Override
    public List<ScBranchRecord> findByTid(Long tid) {
        TransBranchInfoQueryParams transBranchInfoQueryParams = new TransBranchInfoQueryParams();
        transBranchInfoQueryParams.setTid(tid);
        return transBranchInfoMapper.findByConditions(transBranchInfoQueryParams);
    }

    @Override
    public int updateStatusByBranchIdAndStatus(Long branchId, List<Integer> fromStatus,
            Integer toStatus) {
        if (branchId==null || CollectionUtils.isEmpty(fromStatus)
                || toStatus == null) {
            throw new IllegalArgumentException("branchId or fromStatus or toStatus is null");
        }
        return transBranchInfoMapper.updateByBranchIdAndStatus(branchId, fromStatus, toStatus, new Date());
    }

    @Override
    public int updateStatusByBranchId(Long branchId,Integer retryCount, Integer status) {
        return transBranchInfoMapper.updateByBranchId(branchId, status,retryCount, new Date());
    }

    @Override
    public int updateByBranchId(Long branchId, Integer status, Integer retryCount, Date modifyTime) {
        if (StringUtils.isEmpty(branchId) || modifyTime == null) {
            throw new IllegalArgumentException("tid or branchId or modifyTime is null");
        }
        return transBranchInfoMapper.updateByBranchId(branchId,status, retryCount, modifyTime);
    }

    @Override
    public ScBranchRecord findById(Long id) {
        return transBranchInfoMapper.findById(id);
    }

    @Override
    public ScBranchRecord findByTidAndBid(Long tid, Long branchId) {
        return transBranchInfoMapper.findByTidAndBranchId(tid, branchId);
    }

    @Override
    public int updateStatusAndEndTimeById(Long id, Integer status) {
        TransStatus transStatus = TransStatus.getTransStatusByValue(status);
        if (transStatus != TransStatus.COMMIT_FAILED && transStatus != TransStatus.COMMIT_SUCCEED
                && transStatus != TransStatus.CANCEL_FAILED && transStatus != TransStatus.CANCEL_SUCCEED) {
            throw new IllegalStateException("updateStatusAndEndTimeById not support the status");
        }
        ScBranchRecord transBranchInfo = new ScBranchRecord();
        Date now = new Date();
        transBranchInfo.setTid(id);
        transBranchInfo.setStatus(status);
        transBranchInfo.setEndTime(now);
        transBranchInfo.setModifyTime(now);
        return update(transBranchInfo);
    }

    @Override
    public int updateRetryCount(Long id, Integer retryCount) {
        ScBranchRecord transBranchInfo = new ScBranchRecord();
        transBranchInfo.setTid(id);
        transBranchInfo.setRetryCount(retryCount);
        transBranchInfo.setModifyTime(new Date());
        return update(transBranchInfo);
    }

    @Override
    public int updateLocalBranch(Long id, Integer retryCount, Integer status, Date modifyTime, Date endTime) {
        ScBranchRecord transBranchInfo = new ScBranchRecord();
        transBranchInfo.setTid(id);
        transBranchInfo.setRetryCount(retryCount);
        transBranchInfo.setModifyTime(modifyTime);
        transBranchInfo.setStatus(status);
        transBranchInfo.setEndTime(endTime);
        return update(transBranchInfo);
    }

    @Override
    public int getBranchCountByGroupAndGlobalStatus(List<String> groupIdList, List<Integer> statusList) {
        if (CollectionUtils.isEmpty(groupIdList) || CollectionUtils.isEmpty(statusList)) {
            return 0;
        }
        return transBranchInfoMapper.getBranchCountByGroupAndGlobalStatus(groupIdList, statusList,
                DateUtil.getTodayStart());
    }

    @Override
    public int getBranchCountByGroupAndStatus(List<String> groupIdList, List<Integer> statusList) {
        if (CollectionUtils.isEmpty(groupIdList) || CollectionUtils.isEmpty(statusList)) {
            return 0;
        }
        return transBranchInfoMapper.getBranchCountByGroupAndStatus(groupIdList, statusList, DateUtil.getTodayStart());
    }

    @Override
    public int updateStatusAndRetryCount(Long id, List<Integer> fromStatus, Integer toStatus) {
        return transBranchInfoMapper.updateStatusAndRetryCount(id, fromStatus, toStatus, 0, new Date());
    }

    @Override
    public int updateStatusByBids(List<Long> bids, Integer toStatus, Date modifyTime) {
        return transBranchInfoMapper.updateStatusByBids(bids,toStatus,modifyTime);
    }

    private Integer update(ScBranchRecord transBranchInfo) {
        return transBranchInfoMapper.update(transBranchInfo);
    }
}
