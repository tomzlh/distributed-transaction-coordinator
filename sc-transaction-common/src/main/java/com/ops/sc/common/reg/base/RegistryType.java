
package com.ops.sc.common.reg.base;


public enum RegistryType {

    ZK,
    /**
     * Etcd3 registry type
     */
    Etcd3;


    /**
     * Gets type.
     *
     * @param name the name
     * @return the type
     */
    public static RegistryType getType(String name) {
        for (RegistryType registryType : RegistryType.values()) {
            if (registryType.name().equalsIgnoreCase(name)) {
                return registryType;
            }
        }
        throw new IllegalArgumentException("not support registry type: " + name);
    }
}
