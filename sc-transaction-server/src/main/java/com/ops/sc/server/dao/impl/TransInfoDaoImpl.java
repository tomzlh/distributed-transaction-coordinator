package com.ops.sc.server.dao.impl;

import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.utils.DateUtil;
import com.ops.sc.common.bean.Page;
import com.ops.sc.common.bean.TransInfoQueryParams;
import com.ops.sc.mybatis.mapper.TransInfoMapper;
import com.ops.sc.server.dao.TransInfoDao;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@Component
public class TransInfoDaoImpl implements TransInfoDao {

    @Resource
    private TransInfoMapper transInfoMapper;

    @Override
    public void delete(Long tid) {
        transInfoMapper.delete(tid);
    }

    public void save(ScTransRecord transactionInfo) {
        if (transactionInfo.getStatus() == null) {
            transactionInfo.setStatus(TransStatus.TRYING.getValue());
        }
        Date now = new Date();
        transactionInfo.setCreateTime(now);
        transactionInfo.setModifyTime(now);
        transInfoMapper.save(transactionInfo);
    }

    @Override
    public List<ScTransRecord> findByConditions(TransInfoQueryParams transInfoQueryParams) {
        return transInfoMapper.findByConditions(transInfoQueryParams);
    }

    @Override
    public int getTotalCountByConditions(TransInfoQueryParams transInfoQueryParams) {
        if (transInfoQueryParams.getPage() != null) {
            Page page = transInfoQueryParams.getPage();
            transInfoQueryParams.setPage(null);
            int result = transInfoMapper.getCountByConditions(transInfoQueryParams);
            transInfoQueryParams.setPage(page);
            return result;
        }
        return transInfoMapper.getCountByConditions(transInfoQueryParams);
    }

    @Override
    public ScTransRecord findById(Long id) {
        return transInfoMapper.findById(id);
    }

    @Override
    public ScTransRecord findByBusinessId(String businessId) {
        return transInfoMapper.findByBusinessId(businessId);
    }

    @Override
    public ScTransRecord findByTid(Long tid) {
        return transInfoMapper.findByTid(tid);
    }

    @Override
    public List<ScTransRecord> findByStatus(List<Integer> statusList) {
        return transInfoMapper.findByStatus(statusList);
    }

    @Override
    public int updateStatusByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus) {
        if (StringUtils.isEmpty(tid) || fromStatus == null || toStatus == null) {
            throw new IllegalArgumentException("tid or fromStatus or toStatus is null");
        }
        return transInfoMapper.updateStatusByTidAndStatus(tid, fromStatus, toStatus, new Date());
    }

    @Override
    public int updateStatusRetryCountByTidAndStatus(Long tid, Integer fromStatus, Integer toStatus, Integer retryCount) {
        return transInfoMapper.updateStatusAndRetryCount(tid, fromStatus, toStatus,retryCount, new Date());
    }

    @Override
    public int updateStatusByTids(List<Long> tids, Integer status) {
        if (tids==null||tids.isEmpty()||status == null) {
            throw new IllegalArgumentException("tid or status is null");
        }
        return transInfoMapper.updateStatusByTids(tids, status, new Date());
    }

    @Override
    public int updateStatusAndEndTimeById(Long id, Integer status) {
        ScTransRecord dbTransactionInfo = new ScTransRecord();
        dbTransactionInfo.setTid(id);
        dbTransactionInfo.setStatus(status);
        dbTransactionInfo.setModifyTime(new Date());
        dbTransactionInfo.setEndTime(new Date());
        return update(dbTransactionInfo);
    }

    @Override
    public int updateRetryCount(Long id, Integer retryCount) {
        ScTransRecord transactionInfo = new ScTransRecord();
        transactionInfo.setTid(id);
        transactionInfo.setRetryCount(retryCount);
        return transInfoMapper.update(transactionInfo);
    }

    @Override
    public int getTransCountByGroupAndStatus(List<String> groupIdList, List<Integer> statusList) {
        if (CollectionUtils.isEmpty(groupIdList) || CollectionUtils.isEmpty(statusList)) {
            return 0;
        }
        TransInfoQueryParams params = new TransInfoQueryParams();
        params.setCreateTimeStart(DateUtil.getTodayStart());
        params.setCreateTimeEnd(DateUtil.getTodayEnd());
        params.setGroupIdList(groupIdList);
        params.setStatusList(statusList);
        return transInfoMapper.getCountByConditions(params);
    }

    @Override
    public int updateStatusAndInitRetryCount(Long id, Integer fromStatus, Integer toStatus) {
        return transInfoMapper.updateStatusAndRetryCount(id, fromStatus, toStatus, 0, new Date());
    }

    private int update(ScTransRecord transactionInfo) {
        transactionInfo.setModifyTime(new Date());
        return transInfoMapper.update(transactionInfo);
    }
}
