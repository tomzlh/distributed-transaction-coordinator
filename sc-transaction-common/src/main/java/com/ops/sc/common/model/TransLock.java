package com.ops.sc.common.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TransLock {

    private Long id;

    private Long tid;

    private Long branchId;

    private String tableName;

    private String primaryKeyValue; //SQL操作影响的主键值

    private Long createTime;


}
