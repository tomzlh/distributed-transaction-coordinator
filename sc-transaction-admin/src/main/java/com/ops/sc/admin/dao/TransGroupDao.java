package com.ops.sc.admin.dao;

import com.ops.sc.common.bean.TransGroupQueryParams;
import com.ops.sc.common.model.TransGroup;

import java.util.List;


public interface TransGroupDao {

    void save(TransGroup transGroup);


    TransGroup getTransGroupByGroupId(String groupId);

    TransGroup getTransGroupByTenantIdAndGroupName(String tenantId,String groupName);

    List<TransGroup> getTransGroupByTenantId(String tenantId);

    int delete(String groupId);

    int resumeGroup(String groupId);

    int invalidGroup(String groupId);

    List<TransGroup> findByConditions(TransGroupQueryParams transGroupQueryParams);

    int getTransGroupCountByTenantId(String tenantId);
}
