package com.ops.sc.mybatis.mapper;

import com.ops.sc.common.model.ModelDetailDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ModelDetailServiceMapper {

    List<ModelDetailDo> findAllModelsById(@Param("id") Long id);

    List<ModelDetailDo> findModelByModelId(@Param("modelId") Long modelId);



}
