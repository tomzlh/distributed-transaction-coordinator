package com.ops.sc.tc.grpc.sync;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ops.sc.tc.grpc.GrpcClient;
import com.ops.sc.tc.grpc.GrpcInitParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class SponsorGrpcSyncClientBoot {

    private static final Logger LOGGER = LoggerFactory.getLogger(SponsorGrpcSyncClientBoot.class);

    private static final SponsorGrpcSyncClientBoot INSTANCE = new SponsorGrpcSyncClientBoot();
    private static final Integer MAX_SIZE = 100;
    private static final Cache<String, SponsorGrpcSyncClient> TS_CLIENT_MAP = CacheBuilder.newBuilder().maximumSize(MAX_SIZE)
            .removalListener(node -> {
                LOGGER.info("Close server : {} TS Client", node.getKey());
                ((GrpcClient) node.getValue()).shutdownNow();
            }).build();

    private GrpcInitParams tsClientConfig;

    private SponsorGrpcSyncClientBoot() {
    }

    public static SponsorGrpcSyncClientBoot getInstance() {
        return INSTANCE;
    }

    public void initClientConfig(GrpcInitParams tmClientConfig) {
        this.tsClientConfig = tmClientConfig;
    }

    public void createTSClient(String serverAddress) {
        SponsorGrpcSyncClient sponsorGrpcSyncClient = new SponsorGrpcSyncClient();
        if(TS_CLIENT_MAP.getIfPresent(serverAddress)==null) {
            sponsorGrpcSyncClient.init(serverAddress, tsClientConfig);
            TS_CLIENT_MAP.put(serverAddress, sponsorGrpcSyncClient);
        }
    }

   public void closeTSClient(String serverAddress) {
        SponsorGrpcSyncClient sponsorGrpcSyncClient = TS_CLIENT_MAP.getIfPresent(serverAddress);
        if (sponsorGrpcSyncClient == null) {
            LOGGER.warn("Server : {} ts Client can not get in client map", serverAddress);
        }
        TS_CLIENT_MAP.invalidate(serverAddress);
    }

    public Set<String> getRegisterAddress(){
        return TS_CLIENT_MAP.asMap().keySet();
    }

    public SponsorGrpcSyncClient getTSClient(String serverAddress) {
        if (StringUtils.isBlank(serverAddress)) {
            throw new IllegalArgumentException("Get TS client fail, serverAddress is blank");
        }
        try {
            return TS_CLIENT_MAP.get(serverAddress, () -> {
                SponsorGrpcSyncClient sponsorGrpcSyncClient = new SponsorGrpcSyncClient();
                sponsorGrpcSyncClient.init(serverAddress, tsClientConfig);
                return sponsorGrpcSyncClient;
            });
        } catch (ExecutionException e) {
            LOGGER.error("Can not get {} TS client", serverAddress, e);
            throw new IllegalStateException("Get TS client from map fail", e);
        }
    }


    public void shutDown() {
        TS_CLIENT_MAP.asMap().forEach((k, v) -> {
            try {
                v.shutdown();
            } catch (InterruptedException e) {
                LOGGER.error("TS Client:{} shutdown failed.", k, e);
            }
        });
    }
}
