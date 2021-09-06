package com.ops.sc.common.dto.admin;

import com.alibaba.fastjson.annotation.JSONField;


public class AlarmEventInfoDTO {
    @JSONField(name = "AlarmEventName")
    private String name;

    @JSONField(name = "AlarmEventDescription")
    private String description;

    public AlarmEventInfoDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
