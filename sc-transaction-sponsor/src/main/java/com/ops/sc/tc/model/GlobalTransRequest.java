package com.ops.sc.tc.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class GlobalTransRequest implements Serializable {

     private static final long serialVersionUID = -2774534475174704866L;

     private String appName;

     private String transGroupId;

     private String businessId;

     private Long timeout;

     private String transMode;

     private String transCode;

     private String timeoutType;

     private String callMode;

     private String dataSource;

     private boolean callInParallel;

     private int retryCount;

     private List<BranchTransRequest> branchTransRequests;

     @Data
     @ToString
     public static class BranchTransRequest implements Serializable{

          private static final long serialVersionUID = -2674234475174704866L;

          private String branchTransName;
          private String branchName;
          private String transactionName;
          private Map<String, String> branchParamMap;
          private Map<String, String> externalParamMap;
          private boolean hasParent;
          private List<String> parentBranchNames;
          private long timeout;
          private int timeOutType;
          private boolean retryRequired;
          private int retryCount;
          private List<String> returnParams;
     }

}
