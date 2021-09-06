package com.ops.sc.common.bean;

import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
public class GlobalTransRequestBean {

     private String appName;

     private String transGroup;

     private String callerIp;

     private String businessId;

     private String callStrategy;

     private String timeoutType;

     private List<BranchTransRequest> branchTransRequestList;

    @Data
    @ToString
    public class BranchTransRequest{

         private String branchAppName;

         private String branchMethodName;

         private Map<String,Object> paramMap;
    }

}
