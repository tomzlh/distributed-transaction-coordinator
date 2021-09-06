package com.ops.sc.common.bean;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TransGroupQueryParams {
    private Long id;

    private String groupId;

    private String tenantId;

    private String transactionName;

    private Page page;


}
