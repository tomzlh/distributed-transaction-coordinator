package com.ops.sc.server.dao;

import com.ops.sc.common.model.TransMessage;


public interface TransMsgDao {

    void save(TransMessage transMessage);

    TransMessage findByBranchId(Long bid);

    void delete(Long tid);

}
