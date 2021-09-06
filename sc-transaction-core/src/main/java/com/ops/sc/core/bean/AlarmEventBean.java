package com.ops.sc.core.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AlarmEventBean {
    @JSONField(name = "Labels")
    private String groupName;

    @JSONField(name = "Annotations")
    private AlarmMessageBean annotations;

    @JSONField(name = "GeneratorURL")
    private String generatorURL;

    @JSONField(name = "EventName")
    private String eventName;

    public AlarmEventBean(String groupName, AlarmMessageBean annotations, String eventName) {
        this.groupName = groupName;
        this.annotations = annotations;
        this.eventName = eventName;
    }



}
