package com.ops.sc.server.conf;

import lombok.Getter;

public class ServerConfiguration {

      public final static ServerConfiguration instance = InstanceHolder.instance;

      private static final int RPC_SERVER_DEFAULT_PORT = 8888;

      private static final int NETTY_SERVER_DEFAULT_PORT = 9999;

      @Getter
      private String applicationName;

      @Getter
      private String registerType;
      @Getter
      private int rpcServerPort;
      @Getter
      private int nettyServerPort;


      private ServerConfiguration(){
            this.applicationName = ServerPropertyReader.getINSTANCE().getValue("sc.server.applicationName");
            this.registerType = ServerPropertyReader.getINSTANCE().getValue("sc.server.registerType");
            this.rpcServerPort = ServerPropertyReader.getINSTANCE().getIntValue("sc.server.rpcServerPort",RPC_SERVER_DEFAULT_PORT);
            this.nettyServerPort = ServerPropertyReader.getINSTANCE().getIntValue("sc.server.nettyServerPort",NETTY_SERVER_DEFAULT_PORT);
      }


      private static class InstanceHolder {
            static ServerConfiguration instance = new ServerConfiguration();
      }

}
