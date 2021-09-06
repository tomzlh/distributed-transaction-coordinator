package com.ops.sc.compensator.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import com.ops.sc.common.bean.TransInfoQueryParams;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.compensator.service.TransactionOperationService;
import com.ops.sc.compensator.service.CleanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CleanServiceImpl implements CleanService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanServiceImpl.class);


    @Resource
    private TransactionOperationService transactionOperationService;

    @Transactional
    @Override
    public void cleanTransData(Long tid) {
        transactionOperationService.deleteBranch(tid);
        transactionOperationService.deleteGlobal(tid);
    }

    @Override
    @Async("commonTask")
    public void cleanGroupDataAsync(String groupId) {
        LOGGER.info("Start to delete group transaction info, groupId : {} ", groupId);
        TransInfoQueryParams queryParams = new TransInfoQueryParams();
        queryParams.setGroupIdList(Arrays.asList(groupId));
        List<ScTransRecord> transactionInfos = transactionOperationService.find(queryParams);
        transactionInfos.forEach(transInfo -> cleanTransData(transInfo.getTid()));
        LOGGER.info("End to delete transaction info, groupId : {} ", groupId);
    }
}
