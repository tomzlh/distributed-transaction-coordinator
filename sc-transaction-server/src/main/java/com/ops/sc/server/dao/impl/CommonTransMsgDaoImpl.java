package com.ops.sc.server.dao.impl;

import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.core.gather.TransMessageBuilder;
import com.ops.sc.common.model.TransMessage;
import com.ops.sc.common.model.CommonTransMessage;
import com.ops.sc.server.dao.CommonTransMsgDao;
import com.ops.sc.server.dao.TransBranchInfoDao;
import com.ops.sc.server.dao.TransMsgDao;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


@Component
public class CommonTransMsgDaoImpl implements CommonTransMsgDao {

    @Resource
    private TransMsgDao transMsgDao;

    @Resource
    private TransBranchInfoDao transBranchInfoDao;

    @PostConstruct
    public void afterInitialization() {
        //TransMsgDaoFactory.registerBean(BootMode.SERVER, this);
    }

    @Override
    public void delete(Long tid, Long bid) {

    }

    @Override
    public void save(CommonTransMessage commonTransMessage) {
        //transBranchInfoDao.save(TransBranchInfoBuilder.getTransBranchInfo(commonTransMessage));
        transMsgDao.save(TransMessageBuilder.assembleTransMessage(commonTransMessage));
    }

    @Override
    public void updateStatusByTidBranchId(CommonTransMessage commonTransMessage) {
        transBranchInfoDao.updateStatusByBranchId(commonTransMessage.getBid(),
                0,commonTransMessage.getStatus());
    }

    @Override
    public CommonTransMessage findScMessage(Long tid, Long branchId) {
        ScBranchRecord transBranchInfo = transBranchInfoDao.findByTidAndBid(tid, branchId);
        TransMessage transMessage = transMsgDao.findByBranchId(branchId);
        return TransMessageBuilder.assembleGenericTransMessage(transBranchInfo, transMessage);
    }
}
