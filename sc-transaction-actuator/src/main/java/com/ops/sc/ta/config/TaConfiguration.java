package com.ops.sc.ta.config;

import lombok.Getter;

public class TaConfiguration {

      public final static TaConfiguration instance = InstanceHolder.instance;

      @Getter
      private String scanBasicPackage;
      @Getter
      private String dataSource;
      @Getter
      private String applicationName;
      @Getter
      private String transactionServiceGroup;
      @Getter
      private String registerType;



      private TaConfiguration(){
            this.scanBasicPackage = TaPropertyReader.getINSTANCE().getValue("sc.ta.scanBasicPackage");
            this.dataSource = TaPropertyReader.getINSTANCE().getValue("sc.ta.dataSource");
            this.applicationName = TaPropertyReader.getINSTANCE().getValue("sc.ta.applicationName");
            this.transactionServiceGroup = TaPropertyReader.getINSTANCE().getValue("sc.ta.transactionServiceGroup");
            this.registerType = TaPropertyReader.getINSTANCE().getValue("sc.ta.registerType");
      }



      private static class InstanceHolder {
            static TaConfiguration instance = new TaConfiguration();
      }

}
