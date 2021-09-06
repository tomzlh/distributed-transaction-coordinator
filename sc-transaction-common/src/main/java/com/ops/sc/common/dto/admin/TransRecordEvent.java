package com.ops.sc.common.dto.admin;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TransRecordEvent {

    @JSONField(name = "Type")
    private String type;

    @JSONField(name = "StartTime")
    private String startTime;

    @JSONField(name = "EndTime")
    private String endTime;

    @JSONField(name = "Status")
    private Integer status;

    @JSONField(name = "FromTid")
    private Long fromTid;

    @JSONField(name = "ToTid")
    private Long toTid;

    @JSONField(name = "DisplayContent")
    private String displayContent;

    public TransRecordEvent(String typeName, Integer status, Long fromTid, Long toTid) {
        this.type = typeName;
        this.status = status;
        this.fromTid = fromTid;
        this.toTid = toTid;
        this.displayContent = typeName;
    }

    public TransRecordEvent(String type, Integer status, Long fromTid, Long toTid, String displayContent) {
        this.type = type;
        this.status = status;
        this.fromTid = fromTid;
        this.toTid = toTid;
        this.displayContent = displayContent;
    }

    public TransRecordEvent() {
    }


}
