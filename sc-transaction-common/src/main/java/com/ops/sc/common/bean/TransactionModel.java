package com.ops.sc.common.bean;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TransactionModel {

     private Long id;

     private String transCode;

     private String transGroupId;

     private String transMode;

     private String transactionName;

     private String callMode;

     private String modelName;

     private long timeout;

     private int timeoutType;

     private List<ModelDetail> modelDetailList;

     private String desc;

}
