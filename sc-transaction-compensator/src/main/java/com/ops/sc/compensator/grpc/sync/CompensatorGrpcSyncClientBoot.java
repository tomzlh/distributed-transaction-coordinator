package com.ops.sc.compensator.grpc.sync;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ops.sc.compensator.grpc.GrpcClient;
import com.ops.sc.compensator.grpc.GrpcInitParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class CompensatorGrpcSyncClientBoot {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompensatorGrpcSyncClientBoot.class);

    private static final CompensatorGrpcSyncClientBoot INSTANCE = new CompensatorGrpcSyncClientBoot();
    private static final Integer MAX_SIZE = 100;
    private static final Cache<String, CompensatorGrpcSyncClient> TS_CLIENT_MAP = CacheBuilder.newBuilder().maximumSize(MAX_SIZE)
            .removalListener(node -> {
                LOGGER.info("Close server : {} TS Client", node.getKey());
                ((GrpcClient) node.getValue()).shutdownNow();
            }).build();

    private GrpcInitParams tsClientConfig;

    private CompensatorGrpcSyncClientBoot() {
    }

    public static CompensatorGrpcSyncClientBoot getInstance() {
        return INSTANCE;
    }

    public void initClientConfig(GrpcInitParams tmClientConfig) {
        this.tsClientConfig = tmClientConfig;
    }

    public CompensatorGrpcSyncClient getClient(String serverAddress) {
        CompensatorGrpcSyncClient sponsorGrpcSyncClient = new CompensatorGrpcSyncClient();
        if(TS_CLIENT_MAP.getIfPresent(serverAddress)==null) {
            sponsorGrpcSyncClient.init(serverAddress, tsClientConfig);
            TS_CLIENT_MAP.put(serverAddress, sponsorGrpcSyncClient);
        }
        return TS_CLIENT_MAP.getIfPresent(serverAddress);
    }

   public void closeClient(String serverAddress) throws InterruptedException{
        CompensatorGrpcSyncClient grpcSyncClient = TS_CLIENT_MAP.getIfPresent(serverAddress);
        if (grpcSyncClient == null) {
            LOGGER.warn("Server : {} ts Client can not get in client map", serverAddress);
        }
        TS_CLIENT_MAP.invalidate(serverAddress);
        grpcSyncClient.shutdown();
    }

    public Set<String> getRegisterAddress(){
        return TS_CLIENT_MAP.asMap().keySet();
    }



    public void shutDown() {
        TS_CLIENT_MAP.asMap().forEach((k, v) -> {
            try {
                v.shutdown();
            } catch (InterruptedException e) {
                LOGGER.error("Grpc Client:{} shutdown failed.", k, e);
            }
        });
    }
}
