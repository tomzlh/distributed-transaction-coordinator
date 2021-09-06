package com.ops.sc.compensator.grpc;


public abstract class GrpcClient {

    protected static String appName;
    protected String serverAddress;
    protected ChannelFactory channelFactory;


    public static String getAppName() {
        return appName;
    }


    protected void createChannelFactory(String serverAddress, GrpcInitParams config) {
        channelFactory = new ChannelFactory(serverAddress, config);
        appName=config.getAppName();
        channelFactory.init();
    }

    public boolean shutdown() throws InterruptedException {
        return channelFactory.shutdown();
    }

    public void shutdownNow() {
        channelFactory.shutdownNow();
    }
}
