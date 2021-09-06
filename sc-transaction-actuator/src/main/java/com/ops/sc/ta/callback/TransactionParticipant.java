package com.ops.sc.ta.callback;

import java.sql.SQLException;


public interface TransactionParticipant {

    /**
     * 二阶段提交
     *
     * @param tid
     * @param branchId
     * @param extraData
     *            FMT模式下该参数为dataSource,extraData
     * @return
     * @throws SQLException
     */
    Boolean commit(Long tid, Long branchId, String extraData) throws SQLException;

    /**
     * 二阶段回滚
     *
     * @param tid
     * @param branchId
     * @param extraData
     *            FMT模式下该参数为dataSource,extraData
     * @return
     * @throws SQLException
     */
    Boolean rollback(Long tid, Long branchId, String extraData) throws SQLException;
}
