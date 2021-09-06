package com.ops.sc.server.dao.impl;

import com.ops.sc.common.enums.CallErrorCode;
import com.ops.sc.common.model.TransErrorInfo;
import com.ops.sc.mybatis.mapper.TransErrorInfoMapper;
import com.ops.sc.server.dao.TransErrorInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@Component
public class TransErrorInfoDaoImpl implements TransErrorInfoDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransErrorInfoDaoImpl.class);

    @Resource
    private TransErrorInfoMapper transErrorInfoMapper;

    @Override
    public TransErrorInfo findGlobalTransErrorInfoByTid(Long tid) {
        return findErrorInfoByTidAndBranchId(tid, -1L);
    }

    @Override
    public void delete(Long tid) {
        transErrorInfoMapper.delete(tid);
    }

    @Override
    public TransErrorInfo findBranchTransErrorInfoByTidAndBranchId(Long tid, Long branchId) {
        if (branchId==null) {
            throw new IllegalArgumentException();
        }
        return findErrorInfoByTidAndBranchId(tid, branchId);

    }

    private TransErrorInfo findErrorInfoByTidAndBranchId(Long tid, Long branchId) {
        List<TransErrorInfo> transErrorInfoList = transErrorInfoMapper.findBranchTransErrorInfoByTidAndBranchId(tid, branchId);
        if (CollectionUtils.isEmpty(transErrorInfoList)) {
            return null;
        } else if (transErrorInfoList.size() != 1) {
            LOGGER.error("tid : {}, branchId : {}  branch transaction error info from db return more than one result ", tid,
                    branchId);
        }
        return transErrorInfoList.get(0);
    }

    @Override
    public Long saveGlobalTransErrorInfo(Long tid, CallErrorCode callErrorCode, String errorDetail) {
        return save(tid, null, callErrorCode, errorDetail);
    }

    @Override
    public Long saveBranchTransErrorInfo(Long tid, Long branchId, CallErrorCode callErrorCode,
            String errorDetail) {
        return save(tid, branchId, callErrorCode, errorDetail);
    }

    @Override
    public int updateTransErrorInfoById(Long id, CallErrorCode callErrorCode, String errorDetail) {
        return transErrorInfoMapper.updateErrorInfoById(id, callErrorCode.getValue(), errorDetail, new Date());
    }

    private Long save(Long tid, Long branchId, CallErrorCode callErrorCode, String errorDetail) {
        TransErrorInfo transErrorInfo = new TransErrorInfo();
        transErrorInfo.setTid(tid);
        transErrorInfo.setBranchId(branchId);
        transErrorInfo.setErrorType(callErrorCode.getValue());
        transErrorInfo.setErrorDetail(errorDetail);
        transErrorInfo.setCreateTime(new Date());
        transErrorInfo.setModifyTime(new Date());
        transErrorInfoMapper.save(transErrorInfo);
        return transErrorInfo.getId();
    }
}
