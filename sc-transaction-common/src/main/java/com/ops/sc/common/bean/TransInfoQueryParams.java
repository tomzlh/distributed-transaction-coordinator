package com.ops.sc.common.bean;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@ToString
public class TransInfoQueryParams {

    private Long id; // 根据主键ID查询

    private Long maxId; // 最大主键id

    private List<Integer> statusList; // 根据状态查询

    private List<String> groupIdList; // 事务分组ID

    private Long tid;

    private String bizId;

    private String transName;

    private String appName;

    private String instanceName; // 模糊匹配instanceName

    private Date createTimeStart; // 创建时间左区间

    private Date createTimeEnd; // 创建时间右区间

    private Date maxEndTime;
    private Page page;


}
