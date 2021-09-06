package com.ops.sc.common.model;

import lombok.Data;

import java.util.Date;

/**
 * 事务消息表
 */
@Data
public class TransMessage {

    private Long id;

    private Long producerId; // MQ服务器信息标识

    private String metaData; // 统一JSON格式如{"exchange":xxx,"bindKey":xxx}，每次发送时用户指定

    private String payload; // 事务消息体

    private Long bid; // 分支ID

    private Long tid; // 全局事务ID

    private Date createTime;


}
