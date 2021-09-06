package com.ops.sc.common.trans;

import java.sql.SQLException;


public interface TransactionExecutor {

    /**
     * 二阶段提交
     *
     * @param tid
     * @param bid
     * @param extraData
     *            FMT模式下该参数为dataSource,事务消息模式下该参数为uData
     * @return
     * @throws SQLException
     */
    boolean commit(Long tid, Long bid, String extraData) throws SQLException;

    /**
     * 二阶段回滚
     *
     * @param tid
     * @param bid
     * @param extraData
     *            FMT模式下该参数为dataSource,事务消息模式下该参数为uData
     * @return
     * @throws SQLException
     */
    boolean rollback(Long tid, Long bid, String extraData) throws SQLException;
}
