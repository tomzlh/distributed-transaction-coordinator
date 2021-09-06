package com.ops.sc.server.dao;


import com.ops.sc.common.enums.CallErrorCode;
import com.ops.sc.common.model.TransErrorInfo;

public interface TransErrorInfoDao {

    void delete(Long tid);

    TransErrorInfo findGlobalTransErrorInfoByTid(Long tid);

    TransErrorInfo findBranchTransErrorInfoByTidAndBranchId(Long tid, Long branchId);

    Long saveGlobalTransErrorInfo(Long tid, CallErrorCode callErrorCode, String errorDetail);

    Long saveBranchTransErrorInfo(Long tid, Long branchId, CallErrorCode callErrorCode,
                                  String errorDetail);

    int updateTransErrorInfoById(Long id, CallErrorCode callErrorCode, String errorDetail);
}
