package com.ops.sc.common.bean;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@ToString
public class TransMessageQueryParams {

    private Long id; // 根据主键ID查询

    private Long maxId; // 最大主键id

    private List<Integer> statusList; // 根据状态查询

    private List<String> groupIdList; // 事务分组ID

    private String checkBackParam; // 业务id

    private String transName;

    private String tid; // 事务消息所在的全局事务ID

    private String branchId; // 分支事务ID

    private String producerId; // MQ服务器信息唯一标识

    private Date createTimeStart;

    private Date createTimeEnd;

    private Page page;


}
