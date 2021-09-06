package com.ops.sc.tc.grpc;

import lombok.Data;

@Data
public class GrpcInitParams {

    private String appName;
    private int maxChannelCount;
    private long shutdownTimeoutMills;
    private int threadPoolSize;

    public GrpcInitParams(String appName, int maxChannelCount, long shutdownTimeoutMills, int threadPoolSize) {
        this.appName = appName;
        this.maxChannelCount = maxChannelCount;
        this.shutdownTimeoutMills = shutdownTimeoutMills;
        this.threadPoolSize=threadPoolSize;
    }


}