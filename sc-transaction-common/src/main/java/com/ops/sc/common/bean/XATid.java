package com.ops.sc.common.bean;

import javax.transaction.xa.Xid;

import static com.ops.sc.common.constant.Constants.SC_DEFAULT_CHARSET;


public class XATid implements Xid {
    private Long tid;
    private Long branchId;
    private int formatId = 1;

    public XATid(Long tid, Long branchId) {
        this.tid = tid;
        this.branchId = branchId;
    }

    @Override
    public int getFormatId() {
        return formatId;
    }


    @Override
    public byte[] getGlobalTransactionId() {
        String globalTransactionId = String.valueOf(tid);
        return globalTransactionId.getBytes(SC_DEFAULT_CHARSET);
    }

    @Override
    public byte[] getBranchQualifier() {
        return String.valueOf(branchId).getBytes();
    }

    public Long getTid() {
        return tid;
    }

    public Long getBranchId() {
        return branchId;
    }
}
