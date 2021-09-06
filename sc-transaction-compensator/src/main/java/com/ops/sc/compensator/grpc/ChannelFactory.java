package com.ops.sc.compensator.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ChannelFactory {

    private static final String GRPC_DNS_NAME_PREFIX = "dns:///";

    private static final long KEEP_ALIVE_SECOND = 120;

    private List<ManagedChannel> channelList;
    private GrpcInitParams grpcInitParams;
    private String serverAddress;

    public ChannelFactory(String serverAddress, GrpcInitParams grpcInitParams) {
        this.serverAddress = serverAddress;
        this.grpcInitParams = grpcInitParams;
        channelList = new ArrayList<>(grpcInitParams.getMaxChannelCount());
    }

    public void init() {
        for (int i = 0; i < grpcInitParams.getMaxChannelCount(); i++) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(GRPC_DNS_NAME_PREFIX + serverAddress).usePlaintext()
                    .keepAliveTime(KEEP_ALIVE_SECOND, TimeUnit.SECONDS).build();
            channelList.add(channel);
        }
    }

    public ManagedChannel getChannel() {
        return channelList.get(loadBalance());
    }

    boolean shutdown() throws InterruptedException {
        Iterator<ManagedChannel> iterator = channelList.iterator();
        while (iterator.hasNext()) {
            ManagedChannel channel = iterator.next();
            channel.shutdown().awaitTermination(grpcInitParams.getShutdownTimeoutMills(), TimeUnit.MILLISECONDS);
            if (channel.isShutdown()) {
                iterator.remove();
            } else {
                return false;
            }
        }
        return true;
    }

    public void shutdownNow() {
        Iterator<ManagedChannel> iterator = channelList.iterator();
        while (iterator.hasNext()) {
            ManagedChannel channel = iterator.next();
            channel.shutdownNow();
            iterator.remove();
        }
    }

    private int loadBalance() {
        if (grpcInitParams.getMaxChannelCount() == 1) {
            return 0;
        }
        return ThreadLocalRandom.current().nextInt(grpcInitParams.getMaxChannelCount()) % grpcInitParams.getMaxChannelCount();
    }

}
