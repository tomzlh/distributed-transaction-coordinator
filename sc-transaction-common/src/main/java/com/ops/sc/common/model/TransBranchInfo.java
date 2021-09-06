package com.ops.sc.common.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.enums.TransactionType;
import com.ops.sc.common.trans.CommonTransBranchInfo;
import lombok.Data;
import lombok.ToString;

/**
 * 分支事务信息记录表
 *
 */
@Data
@ToString
public class TransBranchInfo extends CommonTransBranchInfo {

    private String businessId;

    private String parentName; // 上一跳分支事务ID

    private Map<String,String> paramMap;

    private Map<String,String> externalMap;

    private List<String> returnParamList;

    private String callerIp; // 分支事务IP地址

    private String branchTransName; // 分支事务名称

    protected Integer retry;

    private Integer retryCount; // 重试次数

    private Integer status; // 分支事务状态

    private Date endTime; // 分支事务结束时间

    private Date createTime;

    private Date modifyTime;

    private String branchName; // 分支应用

    private Long timeout; // 分支事务超时时间

    private Integer timeoutType; // 分支事务超时处理策略

    private String dataSource; // fmt模式下，回调参数，表示confirm/cancel的存储dataSource

    private String transactionName;

    private String transMode;

    private Map<String,String> urlMap;

    private Map<String, List<String>> rollbackParamMap;

    private boolean hasParent;

    private String resourceId; // 资源id。TCC不为空

    private Integer orderNo;
}
