package com.ops.sc.server.service.impl;


import com.ops.sc.common.dto.admin.CallBackErrorInfoResult;
import com.ops.sc.common.enums.CallErrorCode;
import com.ops.sc.common.model.TransErrorInfo;
import com.ops.sc.core.service.ResourceInfoService;
import com.ops.sc.server.dao.TransErrorInfoDao;
import com.ops.sc.server.service.CallBackErrorService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Locale;


@Service
public class CallBackErrorServiceImpl implements CallBackErrorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallBackErrorServiceImpl.class);

    private static final Locale DEFAULT_ERROR_INFO_LOCALE = Locale.CHINESE;

    private static final Integer MAX_ERROR_DETAIL_LENGTH = 255;

    @Resource
    private TransErrorInfoDao transErrorInfoDao;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Override
    public void delete(Long tid) {
        transErrorInfoDao.delete(tid);
    }

    @Override
    @Async("commonTask")
    public void recordCallBackErrorInfoAsync(Long tid, Long branchId, CallErrorCode errorInfo,
            Exception exception) {
        String errorDetail = StringUtils.EMPTY;
        LOGGER.warn("Callback fail, errorCode: {}, tid: {}, branchId: {}", errorInfo.getErrorCode(), tid, branchId);
        if (exception != null) {
            errorDetail = exception.getMessage();
            if (StringUtils.isBlank(errorDetail)) {
                errorDetail = exception.getClass().getName();
            } else {
                errorDetail = errorDetail.substring(0, Math.min(MAX_ERROR_DETAIL_LENGTH, errorDetail.length()));
            }
        }

        TransErrorInfo transErrorInfo;
        if (branchId==null) {
            transErrorInfo = transErrorInfoDao.findGlobalTransErrorInfoByTid(tid);
            if (transErrorInfo == null) {
                transErrorInfoDao.saveGlobalTransErrorInfo(tid, errorInfo, errorDetail);
            } else {
                transErrorInfoDao.updateTransErrorInfoById(transErrorInfo.getId(), errorInfo, errorDetail);
            }
        } else {
            transErrorInfo = transErrorInfoDao.findBranchTransErrorInfoByTidAndBranchId(tid, branchId);
            if (transErrorInfo == null) {
                transErrorInfoDao.saveBranchTransErrorInfo(tid, branchId, errorInfo, errorDetail);
            } else {
                transErrorInfoDao.updateTransErrorInfoById(transErrorInfo.getId(), errorInfo, errorDetail);
            }
        }

        LOGGER.info("Save errorInfo success, tid: {}, branchId: {}", tid, branchId);
    }

    @Override
    public CallBackErrorInfoResult getTransCallBackErrorInfo(Long tid, Long branchId) {
        TransErrorInfo transErrorInfo;
        if (branchId==null) {
            transErrorInfo = transErrorInfoDao.findGlobalTransErrorInfoByTid(tid);
        } else {
            transErrorInfo = transErrorInfoDao.findBranchTransErrorInfoByTidAndBranchId(tid, branchId);
        }
        if (transErrorInfo == null) {
            return new CallBackErrorInfoResult(StringUtils.EMPTY);
        }
        CallErrorCode callErrorCode = CallErrorCode.getCallBackErrorByCodeNum(transErrorInfo.getErrorType());
        String errorMessage = resourceInfoService.getMessage(callErrorCode.getResourceKey(),
                DEFAULT_ERROR_INFO_LOCALE, transErrorInfo.getErrorDetail());
        return new CallBackErrorInfoResult(errorMessage);
    }

}
