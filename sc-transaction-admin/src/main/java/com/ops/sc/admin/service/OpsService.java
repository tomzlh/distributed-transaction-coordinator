package com.ops.sc.admin.service;

import com.ops.sc.common.bean.ResponseResult;


public interface OpsService {


    ResponseResult retryFailBranchTrans(String tenantId, Long tid, Long branchId);


    ResponseResult cancelTimeoutBranchTrans(String tenantId, Long tid, Long branchId);


    ResponseResult checkBackTimeoutGlobalTrans(String tenantId, Long tid);

    ResponseResult getGrpcStreamMap();

}
