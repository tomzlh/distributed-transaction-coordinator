package com.ops.sc.tc.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
public class TransQueryResponse implements Serializable {

     String message;
     String code;
     String businessId;
     String tid;
     String transGroupId;
     String callerIp;
     String appName;
     Long timeout;
     Integer callbackStrategy;
     Integer timeoutType;
     String transMode;
     String dataSource;
     Integer callInParallel;
     String rollbackInfo;
     String transType;
     String callMode;
     String status;
     String create_time;
     String end_time;
     List<TransQueryBranchResponse> branchResponseList;

     @Data
     @ToString
     public static class TransQueryBranchResponse implements Serializable {
          String bid;
          String branchAppName;
          String branchName;
          String transactionName;
          String url;
          String commitMethod;
          String rollbackMethod;
          String branchParam;
          String externalParam;
          Boolean hasParent;
          String parentBranchNames;
          Long timeout;
          Integer timeOutType;
          Boolean retryRequired;
          String rollbackParam;
          String returnParam;
          String status;
          String create_time;
          String end_time;
     }

}

