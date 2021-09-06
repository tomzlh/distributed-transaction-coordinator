package com.ops.sc.compensator.job;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.ops.sc.common.constant.RpcConstants;
import com.ops.sc.common.exception.RpcException;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.compensator.conf.AppConfiguration;
import com.ops.sc.compensator.grpc.GrpcInitParams;
import com.ops.sc.compensator.grpc.sync.CompensatorGrpcSyncClient;
import com.ops.sc.compensator.grpc.sync.CompensatorGrpcSyncClientBoot;
import com.ops.sc.common.reg.zk.ZookeeperRegistryCenter;
import com.ops.sc.compensator.service.TransactionOperationService;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ops.sc.common.enums.TransStatus;
import org.springframework.util.CollectionUtils;


@Component
@ConditionalOnExpression("${sc.server.leader.participate:true}")
public class TransRollbackJob {

    private static final Logger LOGGER = LoggerFactory.getLogger("JOB-LOG");

    private static final long COMPENSATE_JOB_TTL = 25L;

    @Value("${sc.job.lock.key}")
    private String jobLockKey;

    @Value("${sc.trans.retry.count}")
    private Integer retryCount;


    @Resource
    private TransactionOperationService transactionOperationService;

    @Resource
    private AppConfiguration appConfiguration;

    private final String LOCK_PATH="/sc/lock/transrollback";


    private final String SERVER_PATHS="/sc/registry/grpc/";

    private InterProcessMutex lock;


    @Scheduled(cron = "0,15,30,45 * * * * ?")
    public void execute() {
        LOGGER.info("TransRollbackJob start.");
        CompensatorGrpcSyncClient compensatorGrpcSyncClient=null;
        String serverAddress=null;
        try {
            ZookeeperRegistryCenter.getInstance().init();
            lock=new InterProcessMutex(ZookeeperRegistryCenter.getInstance().getClient(), LOCK_PATH);
            lock.acquire(COMPENSATE_JOB_TTL, TimeUnit.MILLISECONDS);
            List<Integer> statusList = Arrays.asList(TransStatus.READY.getValue(),TransStatus.TRYING.getValue(), TransStatus.COMMIT_FAILED.getValue(),
                    TransStatus.CANCEL_FAILED.getValue(), TransStatus.TRY_FAILED.getValue());
            List<String> list= ZookeeperRegistryCenter.getInstance().getChildren(SERVER_PATHS+appConfiguration.getServerCluster());
            if(list!=null&&!list.isEmpty()) {
                serverAddress=list.get(ThreadLocalRandom.current().nextInt(list.size()));
                GrpcInitParams sponsorGrpcSyncClientParams=new GrpcInitParams(appConfiguration.getAppName(), RpcConstants.TA_MAX_CHANNEL_COUNT,RpcConstants.SHUTDOWN_TIMEOUT_MILLS,RpcConstants.DEFAULT_TSCLIENT_THREAD_POOL_SIZE);
                CompensatorGrpcSyncClientBoot compensatorGrpcSyncClientBoot=CompensatorGrpcSyncClientBoot.getInstance();
                compensatorGrpcSyncClientBoot.initClientConfig(sponsorGrpcSyncClientParams);
                compensatorGrpcSyncClient=compensatorGrpcSyncClientBoot.getClient(serverAddress);
            }
            if(compensatorGrpcSyncClient!=null) {
                List<ScTransRecord> scTransRecordList = transactionOperationService.findByStatus(statusList);
                if (!CollectionUtils.isEmpty(scTransRecordList)) {
                    for (ScTransRecord scTransRecord : scTransRecordList) {
                        doCompensate(scTransRecord, compensatorGrpcSyncClient);
                    }
                }
            }
        }catch (Exception e){
            LOGGER.error("TransRollback Job error!",e);
        }
        finally {
            LOGGER.debug("TransRollback Job UnLock...");
            try {
                if(serverAddress!=null){
                    CompensatorGrpcSyncClientBoot.getInstance().closeClient(serverAddress);
                }
            }catch (Exception e){
                LOGGER.error("close the grpc client error!",e);
            }
            try{
                if(lock!=null) {
                    lock.release();
                }
            }catch (Exception e){
                LOGGER.error("release the lock error!",e);
            }
            try{
                ZookeeperRegistryCenter.getInstance().close();
            }catch (Exception e){
                LOGGER.error("close zk connection error!",e);
            }
        }

        LOGGER.info("TransRollbackJob end.");
    }


    @SuppressWarnings("unchecked")
    private void doCompensate(ScTransRecord scTransRecord,CompensatorGrpcSyncClient compensatorGrpcSyncClient)  throws RpcException {
        /*if(scTransRecord.getRetryCount()>retryCount||!transactionOperationService.isGlobalTransTryTimeout(scTransRecord)){
            LOGGER.debug("Transaction execute exceed max count:{}",scTransRecord.getBusinessId());
            return;
        }*/
        transactionOperationService.checkAbnormalGlobalTrans(scTransRecord,compensatorGrpcSyncClient);
    }

}
