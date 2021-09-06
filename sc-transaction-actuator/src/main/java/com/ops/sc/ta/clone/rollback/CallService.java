package com.ops.sc.ta.clone.rollback;



import com.ops.sc.ta.trans.datasource.ScDataSource;

import java.sql.SQLException;



public interface CallService {
    boolean rollback(Long tid, Long bid, ScDataSource scDataSource)
            throws ImageNotConsistentException, SQLException;

    boolean commit(Long tid, Long bid, ScDataSource scDataSource) throws SQLException;
}
