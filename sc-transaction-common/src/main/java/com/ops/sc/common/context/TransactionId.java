package com.ops.sc.common.context;

import lombok.Data;

import java.io.Serializable;

@Data
public class TransactionId implements Serializable {

    private Long tid;

    private Long branchId;

    public TransactionId(Long tid, Long branchId) {
        this.tid = tid;
        this.branchId = branchId;
    }


}
