package com.ops.sc.server.dao;

import com.ops.sc.common.model.CommonTransMessage;


public interface CommonTransMsgDao {

    void save(CommonTransMessage commonTransMessage);

    void updateStatusByTidBranchId(CommonTransMessage commonTransMessage);

    void delete(Long tid, Long bid);

    CommonTransMessage findScMessage(Long tid, Long bid);
}
