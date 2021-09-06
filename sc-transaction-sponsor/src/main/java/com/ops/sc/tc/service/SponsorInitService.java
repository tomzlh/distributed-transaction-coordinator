package com.ops.sc.tc.service;

import com.ops.sc.common.constant.RpcConstants;
import com.ops.sc.common.reg.zk.ZookeeperRegistryCenter;
import com.ops.sc.common.thread.NamedThreadFactory;
import com.ops.sc.tc.conf.TransInfoConfiguration;
import com.ops.sc.tc.grpc.GrpcInitParams;
import com.ops.sc.tc.grpc.sync.SponsorGrpcSyncClient;
import com.ops.sc.tc.grpc.sync.SponsorGrpcSyncClientBoot;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class SponsorInitService {

    private static final long SCHEDULE_DELAY_MILLS = 15 * 1000L;
    private static final long SCHEDULE_INTERVAL_MILLS = 30 * 1000L;

    protected final ScheduledExecutorService timerExecutor = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("timer-sponsor", 1, true));


    private final String SERVER_PATHS="/sc/registry/grpc/";
    @Resource
    private TransInfoConfiguration transInfoConfiguration;

    @Getter
    private List<String> serverList= new ArrayList<>();

    public void init(){
         manageGrpcClient();
         timerExecutor.scheduleAtFixedRate(new Runnable() {
             @Override
             public void run() {
                 manageGrpcClient();
             }
         }, SCHEDULE_DELAY_MILLS, SCHEDULE_INTERVAL_MILLS, TimeUnit.MILLISECONDS);
     }

    private void manageGrpcClient() {
        ZookeeperRegistryCenter.getInstance().init();
        List<String> list= ZookeeperRegistryCenter.getInstance().getChildren(SERVER_PATHS+transInfoConfiguration.getServerCluster());
        SponsorGrpcSyncClientBoot sponsorGrpcSyncClientBoot=SponsorGrpcSyncClientBoot.getInstance();
        GrpcInitParams sponsorGrpcSyncClientParams=new GrpcInitParams(transInfoConfiguration.getAppName(),RpcConstants.TA_MAX_CHANNEL_COUNT,RpcConstants.SHUTDOWN_TIMEOUT_MILLS,RpcConstants.DEFAULT_TSCLIENT_THREAD_POOL_SIZE);
        sponsorGrpcSyncClientBoot.initClientConfig(sponsorGrpcSyncClientParams);
        if(list!=null&&!list.isEmpty()) {
            for(String serverAddress:list) {
                sponsorGrpcSyncClientBoot.createTSClient(serverAddress);
            }
            Set<String> set = sponsorGrpcSyncClientBoot.getRegisterAddress();
            if(set!=null){
                for(String registeredAddress:set){
                    if(!list.contains(registeredAddress)){
                        sponsorGrpcSyncClientBoot.closeTSClient(registeredAddress);
                    }
                }
            }
            serverList=list;
        }
    }
}
