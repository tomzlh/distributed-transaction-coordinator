package com.ops.sc.server.dao.impl;

import com.ops.sc.common.model.TransMessage;
import com.ops.sc.mybatis.mapper.TransMessageMapper;
import com.ops.sc.server.dao.TransMsgDao;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;


@Component
public class TransMsgDaoImpl implements TransMsgDao {

    @Resource
    private TransMessageMapper transMessageMapper;

    @Override
    public void delete(Long tid) {
        transMessageMapper.deleteByTid(tid);
    }

    @Override
    public void save(TransMessage transMessage) {
        transMessage.setCreateTime(new Date());
        transMessageMapper.save(transMessage);
    }

    @Override
    public TransMessage findByBranchId(Long branchId) {
        return transMessageMapper.findByBranchId(branchId);
    }
}
