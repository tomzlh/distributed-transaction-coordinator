package com.ops.sc.admin.grpc.sync;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ops.sc.admin.grpc.GrpcClient;
import com.ops.sc.admin.grpc.GrpcInitParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ScAdminGrpcSyncClientBoot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScAdminGrpcSyncClientBoot.class);

    private static final ScAdminGrpcSyncClientBoot INSTANCE = new ScAdminGrpcSyncClientBoot();
    private static final Integer MAX_SIZE = 100;
    private static final Cache<String, ScAdminGrpcSyncClient> TS_CLIENT_MAP = CacheBuilder.newBuilder().maximumSize(MAX_SIZE)
            .removalListener(node -> {
                LOGGER.info("Close server : {} TS Client", node.getKey());
                ((GrpcClient) node.getValue()).shutdownNow();
            }).build();

    private GrpcInitParams tsClientConfig;

    private ScAdminGrpcSyncClientBoot() {
    }

    public static ScAdminGrpcSyncClientBoot getInstance() {
        return INSTANCE;
    }

    public void initClientConfig(GrpcInitParams tmClientConfig) {
        this.tsClientConfig = tmClientConfig;
    }

    public ScAdminGrpcSyncClient getClient(String serverAddress) {
        ScAdminGrpcSyncClient sponsorGrpcSyncClient = new ScAdminGrpcSyncClient();
        if(TS_CLIENT_MAP.getIfPresent(serverAddress)==null) {
            sponsorGrpcSyncClient.init(serverAddress, tsClientConfig);
            TS_CLIENT_MAP.put(serverAddress, sponsorGrpcSyncClient);
        }
        return TS_CLIENT_MAP.getIfPresent(serverAddress);
    }

   public void closeClient(String serverAddress) throws InterruptedException{
        ScAdminGrpcSyncClient grpcSyncClient = TS_CLIENT_MAP.getIfPresent(serverAddress);
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
