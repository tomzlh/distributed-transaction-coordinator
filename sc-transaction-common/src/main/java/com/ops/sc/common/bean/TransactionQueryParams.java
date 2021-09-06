package com.ops.sc.common.bean;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TransactionQueryParams {

    private Long id; // 根据主键ID查询

    private String groupId; // 事务分组ID,全局唯一

    private String callBackInterface;

    private String callBackMethod;

    private String metaInfo;

    private Page page;

}
