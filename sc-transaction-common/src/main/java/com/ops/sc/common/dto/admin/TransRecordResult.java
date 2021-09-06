package com.ops.sc.common.dto.admin;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.bean.ResponseResult;
import lombok.Data;

@Data
public class TransRecordResult extends ResponseResult {

    @JSONField(name = "AppName")
    private String appName;

    @JSONField(name = "Tid")
    private Long tid;

    @JSONField(name = "StartTime")
    private String startTime;

    @JSONField(name = "EndTime")
    private String endTime;

    @JSONField(name = "Status")
    private Integer status;

    @JSONField(name = "Children")
    private List<Children> childrenList;



    @Data
    public static class Children {

        @JSONField(name = "AppName")
        private String appName;

        @JSONField(name = "Tid")
        private Long tid;

        @JSONField(name = "BranchId")
        private Long branchId;

        @JSONField(name = "ParentId")
        private String parentId;

        @JSONField(name = "TransactionName")
        private String transactionName;

        @JSONField(name = "TransType")
        private String transType;

        @JSONField(name = "Status")
        private String status;



    }

}
