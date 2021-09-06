package com.ops.sc.compensator.job;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.ops.sc.common.bean.TransInfoQueryParams;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.reg.zk.ZookeeperRegistryCenter;
import com.ops.sc.compensator.service.TransactionOperationService;
import com.ops.sc.compensator.service.CleanService;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ops.sc.common.enums.TransStatus;

@Component
@ConditionalOnExpression("${sc.server.clean.participate:true}")
public class DataCleanJob {

    private static final Logger LOGGER = LoggerFactory.getLogger("JOB-LOG");

    private static final long DATA_CLEAN_JOB_TTL = 30L;


    @Value("${sc.job.clean.retainMonth:3}")
    private Integer retainMonth;

    @Resource
    private TransactionOperationService transactionOperationService;

    @Resource
    private CleanService cleanService;

    private final String LOCK_PATH="/sc/lock/dataclean";

    private  InterProcessMutex lock;

    @Scheduled(cron = "${sc.job.clean.cron}")
    public void execute() {
        LOGGER.info("Data clean job start.");
        ZookeeperRegistryCenter.getInstance().init();
        lock=new InterProcessMutex(ZookeeperRegistryCenter.getInstance().getClient(), LOCK_PATH);
        try {
            lock.acquire(DATA_CLEAN_JOB_TTL, TimeUnit.MILLISECONDS);
            clean();
        }catch (Exception e){
            LOGGER.error("fail to execute data clean!", e);
        }
        finally {
            LOGGER.debug("Data clean job unLock...");
            try {
                lock.release();
            }catch (Exception e){
                LOGGER.error("failed to release lock!", e);
            }
            try{
                ZookeeperRegistryCenter.getInstance().close();
            }catch (Exception e){
                LOGGER.error("close zk connection error!",e);
            }
        }
        LOGGER.info("Data clean job end.");
    }

    private void clean() {
        TransInfoQueryParams params = new TransInfoQueryParams();
        params.setStatusList(Arrays.asList(TransStatus.COMMIT_SUCCEED.getValue(), TransStatus.CANCEL_SUCCEED.getValue()));
        ZonedDateTime zonedDateTime = ZonedDateTime.now().minusMonths(retainMonth);
        params.setMaxEndTime(Date.from(zonedDateTime.toInstant()));
        List<ScTransRecord> transactionInfoList = transactionOperationService.find(params);
        LOGGER.info("Execute data clean, the size : {}  before date: {} ", transactionInfoList.size(), zonedDateTime);
        transactionInfoList.forEach(transInfo -> {
            Long tid = transInfo.getTid();
            LOGGER.debug("Clean data tid : {} , createTime : {} ", tid, dateFormatter(transInfo.getCreateTime()));
            cleanService.cleanTransData(tid);
        });
    }

    private String dateFormatter(Date date) {
        LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
    }

}
