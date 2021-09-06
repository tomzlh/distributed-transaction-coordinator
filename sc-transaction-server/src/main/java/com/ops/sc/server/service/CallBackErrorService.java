package com.ops.sc.server.service;


import com.ops.sc.common.dto.admin.CallBackErrorInfoResult;
import com.ops.sc.common.enums.CallErrorCode;

public interface CallBackErrorService {

    void recordCallBackErrorInfoAsync(Long tid, Long branchId, CallErrorCode errorInfo,
            Exception exception);

    default void recordCallBackErrorInfoAsync(Long tid, Long branchId, CallErrorCode errorInfo) {
        recordCallBackErrorInfoAsync(tid, branchId, errorInfo, null);
    }

    void delete(Long tid);

    default void recordCallBackErrorInfoAsync(Long tid, CallErrorCode errorInfo, Exception exception) {
        recordCallBackErrorInfoAsync(tid, null, errorInfo, exception);
    }

    default void recordCallBackErrorInfoAsync(Long tid, CallErrorCode errorInfo) {
        recordCallBackErrorInfoAsync(tid, null, errorInfo, null);
    }

    CallBackErrorInfoResult getTransCallBackErrorInfo(Long tid, Long branchId);
}
