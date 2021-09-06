package com.ops.sc.common.model;

import com.ops.sc.common.trans.CommonTransBranchInfo;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CommonTransMessage extends CommonTransBranchInfo {

    private String id; // 主键ID

    private String producerName; // 生产者标识

    private String transactionName;

    private String metaData; // 统一JSON格式如{"exchange":xxx,"bindKey":xxx}，每次发送时用户指定

    private String payload; // 事务消息体

    private Integer status; // 状态

    private Long createTime;

    private String parentId; // 上一跳分支事务ID

    private String clientLocalIp; // 分支事务IP地址

    private String appName;

    private String businessId;


}
