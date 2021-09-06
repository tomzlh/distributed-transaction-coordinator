
package com.ops.sc.core.rest.config;


public enum NetWorkProType {
    /**
     * Tcp transport protocol type.
     */
    TCP("tcp"),

    /**
     * Unix domain socket transport protocol type.
     */
    UNIX_DOMAIN_SOCKET("unix-domain-socket");

    /**
     * The Name.
     */
    public final String name;

    NetWorkProType(String name) {
        this.name = name;
    }

    /**
     * Gets type.
     *
     * @param name the name
     * @return the type
     */
    public static NetWorkProType getType(String name) {
        name = name.trim().replace('-', '_');
        for (NetWorkProType b : NetWorkProType.values()) {
            if (b.name().equalsIgnoreCase(name)) {
                return b;
            }
        }
        throw new IllegalArgumentException("unknown type:" + name);
    }
}
