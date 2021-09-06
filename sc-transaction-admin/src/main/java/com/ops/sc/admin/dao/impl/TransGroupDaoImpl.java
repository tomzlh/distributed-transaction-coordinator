package com.ops.sc.admin.dao.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.ops.sc.admin.dao.TransGroupDao;
import com.ops.sc.common.bean.TransGroupQueryParams;
import com.ops.sc.common.model.TransGroup;
import org.springframework.stereotype.Component;

import com.ops.sc.mybatis.mapper.TransGroupMapper;


@Component
public class TransGroupDaoImpl implements TransGroupDao {

    @Resource
    private TransGroupMapper transGroupMapper;

    public void save(TransGroup transGroup) {
        transGroup.setCreateTime(new Date());
        transGroup.setModifyTime(new Date());
        transGroupMapper.save(transGroup);
    }



    public TransGroup getTransGroupByGroupId(String groupId) {
        return transGroupMapper.getTransGroupByGroupId(groupId);
    }

    @Override
    public TransGroup getTransGroupByTenantIdAndGroupName(String tenantId, String groupName) {
        return transGroupMapper.getTransGroupByTenantIdAndGroupName(tenantId,groupName);
    }

    @Override
    public List<TransGroup> getTransGroupByTenantId(String tenantId) {
        return transGroupMapper.getGroupByTenantId(tenantId);
    }


    public int delete(String groupId) {
        return transGroupMapper.delete(groupId);
    }

    public int invalidGroup(String groupId) {
        return transGroupMapper.invalidGroup(groupId);
    }

    public int resumeGroup(String groupId) {
        return transGroupMapper.resumeGroup(groupId);
    }

    public List<TransGroup> findByConditions(TransGroupQueryParams transGroupQueryParams) {
        return transGroupMapper.findByConditions(transGroupQueryParams);
    }

    public int getTransGroupCountByTenantId(String tenantId) {
        List<TransGroup> groupList = transGroupMapper.getGroupByTenantId(tenantId);
        return groupList==null?0:groupList.size();
    }
}
