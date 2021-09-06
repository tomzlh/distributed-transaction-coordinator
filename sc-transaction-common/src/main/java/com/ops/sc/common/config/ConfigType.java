package com.ops.sc.common.config;

public enum ConfigType {

    /**
     * File config type.
     */
    File,
    /**
     * zookeeper config type.
     */
    ZK,

    Etcd3;


    public static ConfigType getType(String name) {
        for (ConfigType configType : values()) {
            if (configType.name().equalsIgnoreCase(name)) {
                return configType;
            }
        }
        throw new IllegalArgumentException("not support config type: " + name);
    }
}
