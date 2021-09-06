package com.ops.sc.server.dao.impl;

import com.ops.sc.common.model.TransGroup;
import com.ops.sc.common.bean.TransGroupQueryParams;
import com.ops.sc.mybatis.mapper.TransGroupMapper;
import com.ops.sc.server.dao.TransGroupDao;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@Component
public class TransGroupDaoImpl implements TransGroupDao {

    @Resource
    private TransGroupMapper transGroupMapper;

    @Override
    public void save(TransGroup transGroup) {
        transGroup.setCreateTime(new Date());
        transGroup.setModifyTime(new Date());
        transGroupMapper.save(transGroup);
    }
    @Override
    public List<TransGroup> getAllValidTransGroup(){
        return transGroupMapper.getAllValidTransGroup();
    }

    @Override
    public TransGroup getTransGroupByGroupId(String groupId) {
        return transGroupMapper.getTransGroupByGroupId(groupId);
    }
    @Override
    public List<TransGroup> getTransGroupByTenantId(String tenantId) {
        return transGroupMapper.getGroupByTenantId(tenantId);
    }
    @Override
    public int delete(String groupId) {
        return transGroupMapper.delete(groupId);
    }
    @Override
    public int invalidGroup(String groupId) {
        return transGroupMapper.invalidGroup(groupId);
    }
    @Override
    public List<TransGroup> findByConditions(TransGroupQueryParams transGroupQueryParams) {
        return transGroupMapper.findByConditions(transGroupQueryParams);
    }
}
