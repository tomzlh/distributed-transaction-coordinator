package com.ops.sc.core.config;

import com.ops.sc.common.enums.BootMode;


public class TransMessageConfig {

    // Enable的插件信息
    private static String enablePluginList = "";

    private static BootMode bootMode = BootMode.CLIENT;

    public static String getEnablePluginList() {
        return enablePluginList;
    }

    public static void setEnablePluginList(String enablePluginList) {
        TransMessageConfig.enablePluginList = enablePluginList;
    }

    public static void setRunInServerMode() {
        bootMode = BootMode.SERVER;
    }

    public static boolean isRunInServerMode() {
        return bootMode.equals(BootMode.SERVER);
    }

    public static boolean isRunInClientMode() {
        return bootMode.equals(BootMode.CLIENT);
    }

    public static BootMode getRunMode() {
        return bootMode;
    }

}
