package com.ops.sc.tc.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GlobalRollbackRequest {

    private String businessId;
    private String tid;
    private String transMode;
    private boolean isSync;
    private Long timeout;
    private int timeOutType;
}
