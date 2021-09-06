package com.ops.sc.common.bean;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BranchRollbackRequest extends ScRequestMessage {

    protected String businessId;

    protected long tid;

    /**
     * The Branch id.
     */
    protected long branchId;

    //xa模式下使用
    protected String dataSource;


    /**
     * The Application data.
     */
    protected String applicationData;


}
