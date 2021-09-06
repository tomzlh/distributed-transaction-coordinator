package com.ops.sc.tc.grpc.async;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ops.sc.tc.grpc.GrpcClient;
import com.ops.sc.tc.grpc.GrpcInitParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SponsorGrpcAsyncClientBoot {
    private static final Logger LOGGER = LoggerFactory.getLogger(SponsorGrpcAsyncClientBoot.class);

    private static final SponsorGrpcAsyncClientBoot INSTANCE = new SponsorGrpcAsyncClientBoot();
    private static final Integer MAX_SIZE = 100;
    private static final Cache<String, SponsorGrpcAsyncClient> CLIENT_MAP = CacheBuilder.newBuilder().maximumSize(MAX_SIZE)
            .removalListener(node -> {
                LOGGER.info("Close server : {} TSClient", node.getKey());
                ((GrpcClient) node.getValue()).shutdownNow();
            }).build();
    private GrpcInitParams grpcClientParams;

    private SponsorGrpcAsyncClientBoot() {
    }

    public static SponsorGrpcAsyncClientBoot getInstance() {
        return INSTANCE;
    }

    public void initClientConfig(GrpcInitParams rmClientConfig) {
        this.grpcClientParams = rmClientConfig;
    }

    public void initTSClient(String serverAddress) {
        SponsorGrpcAsyncClient sponsorGrpcAsyncClient = new SponsorGrpcAsyncClient();
        sponsorGrpcAsyncClient.init(serverAddress, grpcClientParams);
        CLIENT_MAP.put(serverAddress, sponsorGrpcAsyncClient);
    }

    void closeTSClient(String serverAddress) {
        SponsorGrpcAsyncClient sponsorGrpcAsyncClient = CLIENT_MAP.getIfPresent(serverAddress);
        if (sponsorGrpcAsyncClient == null) {
            LOGGER.warn("Server : {} tsClient  can not get in client map", serverAddress);
        }
        CLIENT_MAP.invalidate(serverAddress);
    }


    public SponsorGrpcAsyncClient getTSClient(String serverAddress) {
        if (StringUtils.isBlank(serverAddress)) {
            throw new IllegalArgumentException("Get TS client fail, serverAddress is blank");
        }

        return CLIENT_MAP.getIfPresent(serverAddress);

    }


    public void shutDown() {
        CLIENT_MAP.asMap().forEach((k, v) -> {
            try {
                v.shutdown();
            } catch (InterruptedException e) {
                LOGGER.error("TSClient:{} shutdown fail.", k, e);
            }
        });
    }
}
