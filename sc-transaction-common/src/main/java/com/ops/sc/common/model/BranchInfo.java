package com.ops.sc.common.model;


import com.ops.sc.common.constant.Constants;
import lombok.Data;

@Data
public class BranchInfo {

    private Long id;

    private Long tid;

    private Long bid;

    private String parentId; // 上一跳分支事务ID


    private Integer retryCount; // 重试次数

    private Integer status; // 分支事务状态

    private Long endTime; // 分支事务结束时间

    private Long createTime;

    private Long modifyTime;

    private String transactionName; // 分支事务别名

    private Long timeout; // 分支事务超时时间

    private Integer timeoutType; // 分支事务超时处理策略

    private Integer transType; // 分支事务类型。0表示TCC，2表示FMT

    private byte[] params;

    private String resourceId; // 资源id。TCC不为空

    public boolean exceedMaxTime() {
        return System.currentTimeMillis() - modifyTime > Constants.LOCAL_DEFAULT_TIMEOUT;
    }

    public boolean isBranchTryTimeout() {
        return (System.currentTimeMillis() - modifyTime) > timeout;
    }


}
