package com.ops.sc.common.lb;

public enum  LoadBalanceType {

      ROUNDROBIN,

      CONSITENTHASH,

      LEASTACTIVE,

      RANDOM;

    public static LoadBalanceType getType(String name) {
        for (LoadBalanceType loadBalanceType : LoadBalanceType.values()) {
            if (loadBalanceType.name().equalsIgnoreCase(name)) {
                return loadBalanceType;
            }
        }
        throw new IllegalArgumentException("not support loadBalance type: " + name);
    }
}
