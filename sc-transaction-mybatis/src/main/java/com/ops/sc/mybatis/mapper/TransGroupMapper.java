package com.ops.sc.mybatis.mapper;

import com.ops.sc.common.bean.TransGroupQueryParams;
import com.ops.sc.common.model.TransGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface TransGroupMapper {

    void save(TransGroup transGroup);

    int update(TransGroup transGroup);

    TransGroup getTransGroupByGroupId(@Param("groupId") String groupId);

    TransGroup getTransGroupByTenantIdAndGroupName(@Param("tenantId") String tenantId,@Param("groupName") String groupName);


    List<TransGroup> getGroupByTenantId(@Param("tenantId") String tenantId);


    List<TransGroup> getAllValidTransGroup();

    int delete(@Param("groupId") String groupId);

    int invalidGroup(@Param("groupId") String groupId);

    int resumeGroup(@Param("groupId") String groupId);

    List<TransGroup> findByConditions(TransGroupQueryParams transGroupQueryParams);

}
