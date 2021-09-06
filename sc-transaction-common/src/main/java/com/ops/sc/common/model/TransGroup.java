package com.ops.sc.common.model;

import lombok.Data;

import java.util.Date;

@Data
public class TransGroup {

    private Long id; // 主键ID

    private String groupName; // 事务分组名称

    private String groupId; // 事务分组ID

    private String tenantId; // 租户id

    private Date createTime;

    private Date modifyTime;

}
