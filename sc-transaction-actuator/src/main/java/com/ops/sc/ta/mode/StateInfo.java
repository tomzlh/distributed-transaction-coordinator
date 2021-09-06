package com.ops.sc.ta.mode;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StateInfo {

    private Long tid;

    private Long branchId;

    public StateInfo() {
    }

    public StateInfo(Long tid, Long branchId) {
        this.tid = tid;
        this.branchId = branchId;
    }


}
