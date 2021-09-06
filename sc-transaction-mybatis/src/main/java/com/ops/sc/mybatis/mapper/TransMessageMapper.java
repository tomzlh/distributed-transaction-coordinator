package com.ops.sc.mybatis.mapper;

import com.ops.sc.common.model.TransMessage;
import org.apache.ibatis.annotations.Param;


public interface TransMessageMapper {

    void save(TransMessage transMessage);

    TransMessage findByBranchId(@Param("branchId") Long branchId);

    void deleteByTid(@Param("tid") Long tid);

}
