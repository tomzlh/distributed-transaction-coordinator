package com.ops.sc.common.enums;

import java.util.HashMap;
import java.util.Map;


public enum HttpMethod {
    GET, POST, PUT, DELETE;

    static final Map<String, HttpMethod> METHOD_MAP = new HashMap<>(4);

    static {
        for (HttpMethod method : HttpMethod.values()) {
            METHOD_MAP.put(method.toString(), method);
        }
    }

    public static HttpMethod fromValue(String method) {
        return METHOD_MAP.get(method.toUpperCase());
    }

}
