package com.ops.sc.server.service.impl;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.CallBackType;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.server.service.CallAction;
import com.ops.sc.server.dao.TransBranchInfoDao;
import com.ops.sc.server.service.BranchTransService;
import com.ops.sc.common.enums.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class BranchTransServiceImpl implements BranchTransService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchTransServiceImpl.class);

    @Resource
    private TransBranchInfoDao transBranchInfoDao;

    @Autowired
    private CallAction callAction;

    @Override
    public void save(ScBranchRecord transBranchInfo) {
        transBranchInfoDao.save(transBranchInfo);
    }

    @Override
    public void save(List<ScBranchRecord> transBranchInfoList) {
        transBranchInfoDao.save(transBranchInfoList);
    }


    @Override
    public void delete(Long tid) {
        transBranchInfoDao.delete(tid);
    }

    @Override
    public List<ScBranchRecord> getTransBranchInfoList(Long tid) {
        return transBranchInfoDao.findByTid(tid);
    }


    @Override
    public Boolean isBranchTransTryTimeout(ScBranchRecord transBranchInfo) {
        if (TransStatus.getTransStatusByValue(transBranchInfo.getStatus()) != TransStatus.TRYING) {
            throw new IllegalStateException();
        }
        Date modifyTime = transBranchInfo.getModifyTime();
        Date now = new Date();
        return (now.getTime() - modifyTime.getTime()) > transBranchInfo.getTimeout();
    }

    @Override
    public void updateFailBranchTransRetryCountAndStatus(ScBranchRecord transBranchInfo) {
        TransStatus branchStatus = TransStatus.getTransStatusByValue(transBranchInfo.getStatus());
        if (branchStatus != TransStatus.CANCEL_FAILED && branchStatus != TransStatus.COMMIT_FAILED) {
            throw new IllegalStateException(" not supported status when retry branch");
        }
        TransStatus targetStatus = branchStatus == TransStatus.CANCEL_FAILED ? TransStatus.CANCELLING : TransStatus.COMMITTING;
        transBranchInfoDao.updateStatusAndRetryCount(transBranchInfo.getId(),
                Collections.singletonList(transBranchInfo.getStatus()), targetStatus.getValue());
        LOGGER.info("tid : {}, branchId : {} update status and init retry count to 0 success", transBranchInfo.getTid(),
                transBranchInfo.getBid());
    }


    @Override
    public Boolean isNeedExecute(ScBranchRecord transBranchInfo, TransStatus globalTransStatus, Long singleTimeout) {

        TransStatus branchStatus = TransStatus.getTransStatusByValue(transBranchInfo.getStatus());
        if (branchStatus == TransStatus.TRY_SUCCEED || branchStatus == TransStatus.TRY_FAILED) {
            TransStatus targetTransStatus = globalTransStatus == TransStatus.COMMITTING ? TransStatus.COMMITTING : TransStatus.CANCELLING;
            int dbUpdateResult = (transBranchInfoDao.updateStatusByBranchIdAndStatus(transBranchInfo.getBid(), Collections.singletonList(transBranchInfo.getStatus()),
                    targetTransStatus.getValue()));
            return dbUpdateResult > 0;
        }

        if (branchStatus == TransStatus.CANCELLING || branchStatus == TransStatus.COMMITTING) {
            // 判断confirm/cancel是否超时
            Date now = new Date();
            // TIMEOUT_TOLERANT是节点之间时间不同步的余量
            Long timeout = singleTimeout + Constants.TIMEOUT_TOLERANT;
            Long stayTime = now.getTime() - transBranchInfo.getModifyTime().getTime();
            if (stayTime > timeout) {
                // 已经超时，需要重新执行
                int dbUpdateResult = transBranchInfoDao.updateByBranchId(transBranchInfo.getBid(),
                        branchStatus.getValue(), 0, transBranchInfo.getModifyTime());
                if (dbUpdateResult > 0) {
                    LOGGER.info("branchId : {} status : {} duration : {} ms, timeoutMills, need to recall",
                            transBranchInfo.getBid(), branchStatus, stayTime);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 分支事务执行confirm/cancel
     *
     * @param transBranchInfo
     * @param callBackType
     * @return
     */
    @Override
    public ExecutionResult executeBranchTrans(ScBranchRecord transBranchInfo, CallBackType callBackType) {
        return callAction.branchExecute(transBranchInfo, callBackType);
    }

    @Override
    public int updateStatusById(Long id, Integer status,Integer retryCount,Date modifyTime) {
        return transBranchInfoDao.updateByBranchId(id, status, retryCount,modifyTime);
    }

    @Override
    public int updateRetryCount(Long id, Integer retryCount) {
        return transBranchInfoDao.updateRetryCount(id, retryCount);
    }

    @Override
    public int updateLocalBranch(Long id, Integer retryCount, Integer status, Date modifyTime, Date endTime) {
        return transBranchInfoDao.updateLocalBranch(id, retryCount, status, modifyTime, endTime);
    }

    @Override
    public ScBranchRecord findByTidAndBid(Long tid, Long branchId) {
        return transBranchInfoDao.findByTidAndBid(tid, branchId);
    }

    @Override
    public int updateStatusByBidAndStatus(Long branchId, List<Integer> fromStatus,
                                          Integer toStatus) {
        return transBranchInfoDao.updateStatusByBranchIdAndStatus(branchId, fromStatus, toStatus);
    }

    @Override
    public int updateStatusByBids(List<Long> bids, Integer toStatus,Date modifyTime) {
        return transBranchInfoDao.updateStatusByBids(bids,toStatus,modifyTime);
    }
}
