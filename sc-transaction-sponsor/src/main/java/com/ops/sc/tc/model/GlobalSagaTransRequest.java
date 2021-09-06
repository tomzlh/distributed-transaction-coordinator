package com.ops.sc.tc.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class GlobalSagaTransRequest implements Serializable {

     private static final long serialVersionUID = -2774534475174704866L;

     private String appName;

     private String businessId;

     private String transCode;

     private List<BranchSagaTransRequest> branchTransRequests;

     @Data
     @ToString
     public static class BranchSagaTransRequest implements Serializable{
          private static final long serialVersionUID = -2674234475174704866L;
          private String modelBranchName;
          private Map<String, String> branchParamMap;
          private Map<String, String> externalParamMap;
     }

}
