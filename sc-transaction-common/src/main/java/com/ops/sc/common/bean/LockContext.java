package com.ops.sc.common.bean;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class LockContext {

    private String resourceId;

    private Map<String, Set<String>> operateMap;

    private Boolean requireLock;

    public LockContext() {
    }

    public LockContext(String resourceId, Map<String, Set<String>> operateMap, Boolean requireLock) {
        this.resourceId = resourceId;
        this.operateMap = operateMap;
        this.requireLock = requireLock;
    }


}
