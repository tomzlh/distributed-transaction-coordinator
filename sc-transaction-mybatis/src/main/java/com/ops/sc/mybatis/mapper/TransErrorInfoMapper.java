package com.ops.sc.mybatis.mapper;

import java.util.Date;
import java.util.List;

import com.ops.sc.common.model.TransErrorInfo;
import org.apache.ibatis.annotations.Param;



public interface TransErrorInfoMapper {

    List<TransErrorInfo> findBranchTransErrorInfoByTidAndBranchId(@Param("tid") Long tid,
                                                                  @Param("branchId") Long branchId);

    int save(TransErrorInfo transErrorInfo);

    void delete(@Param("tid") Long tid);

    int updateErrorInfoById(@Param("id") Long id, @Param("errorType") Integer errorType,
            @Param("errorDetail") String detail, @Param("modifyTime") Date modifyTime);

}
