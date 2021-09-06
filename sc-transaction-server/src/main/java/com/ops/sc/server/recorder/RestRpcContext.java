package com.ops.sc.server.recorder;

import com.ops.sc.common.enums.Participant;
import lombok.Data;

@Data
public class RestRpcContext {

     private Participant ClientRole;

     private String clientId;

     private String applicationName;

     private String transactionServiceGroup;

     private String url;
}
