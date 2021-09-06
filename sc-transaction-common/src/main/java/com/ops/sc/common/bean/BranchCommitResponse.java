package com.ops.sc.common.bean;

import com.ops.sc.common.enums.TransRecordType;
import lombok.Data;

@Data
public class BranchCommitResponse extends ScResponseMessage {

    protected String businessId;

    protected long tid;

    /**
     * The Branch id.
     */
    protected long branchId;
    /**
     * The Branch status.
     */
    protected TransRecordType transRecordType;


}
