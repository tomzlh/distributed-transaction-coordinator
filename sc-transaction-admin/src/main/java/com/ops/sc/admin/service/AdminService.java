package com.ops.sc.admin.service;

import com.ops.sc.common.bean.FailureTransInfoRequestParams;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.dto.admin.FailedTransInfoListResult;
import com.ops.sc.common.dto.admin.AlarmEventResultList;
import com.ops.sc.common.dto.admin.BranchInfosResult;
import com.ops.sc.common.dto.admin.TransInfosResult;
import com.ops.sc.common.dto.admin.TransSummaryResult;
import com.ops.sc.common.dto.admin.TransRecordResult;
import com.ops.sc.common.bean.TransInfoRequestQueryParams;


public interface AdminService {

    TransSummaryResult getTransSummary(String tenantId);

    ResponseResult cleanTransResource(String groupId);

    TransInfosResult getTccInfos(String tenantId, TransInfoRequestQueryParams queryParams);

    BranchInfosResult getBranchInfos(String tenantId, Long tid);

    AlarmEventResultList getAlarmEventInfos(String filterEventName);

    FailedTransInfoListResult getFailedTccInfos(FailureTransInfoRequestParams params);

    TransRecordResult getTransRecord(Long tid);

}
