package com.ops.sc.mybatis.mapper;

import com.ops.sc.common.model.TransactionModelDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ModelServiceMapper {

    TransactionModelDo findByTransCode(@Param("transCode") String transCode);

    List<TransactionModelDo> loadAllTransModel();

}
