package com.ops.sc.core.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class AlarmMessageBean {

    @JSONField(name = "message")
    private String message;

    public AlarmMessageBean(String message) {
        this.message = message;
    }

}
