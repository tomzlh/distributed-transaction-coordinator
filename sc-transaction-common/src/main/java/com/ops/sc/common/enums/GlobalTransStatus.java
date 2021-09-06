package com.ops.sc.common.enums;

import org.apache.commons.lang3.StringUtils;


public enum GlobalTransStatus {

    SUCCESS("全局事务成功"),
    TRYING("全局事务进行中"),
    FAILED("全局事务失败"),
    NOT_EXIST("全局事务不存在");

    private String desc;

    GlobalTransStatus(String desc) {
        this.desc = desc;
    }

    /**
     * @param name
     * @return
     */
    public static GlobalTransStatus getCheckBackResultEnumByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (GlobalTransStatus globalTransStatus : GlobalTransStatus.values()) {
            if (name.contains(globalTransStatus.name())) {
                return globalTransStatus;
            }
        }
        return null;
    }
}
