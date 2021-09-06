package com.ops.sc.server.dao;

import com.ops.sc.common.model.TransGroup;
import com.ops.sc.common.bean.TransGroupQueryParams;

import java.util.List;


public interface TransGroupDao {

    void save(TransGroup transGroup);

    TransGroup getTransGroupByGroupId(String groupId);

    List<TransGroup> getTransGroupByTenantId(String tenantId);

    int delete(String groupId);

    List<TransGroup> getAllValidTransGroup();

    int invalidGroup(String groupId);

    List<TransGroup> findByConditions(TransGroupQueryParams transGroupQueryParams);

}
