package com.ops.sc.common.trans;


public class CommonTransBranchInfo extends CommonTransInfo {

    private Long bid;

    public CommonTransBranchInfo() {
    }

    public CommonTransBranchInfo(Long tid, Long bid) {
        super(tid);
        this.bid = bid;
    }

    public Long getBid() {
        return bid;
    }

    public void setBid(Long bid) {
        this.bid = bid;
    }
}
