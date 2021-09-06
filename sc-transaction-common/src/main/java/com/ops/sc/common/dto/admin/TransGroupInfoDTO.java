package com.ops.sc.common.dto.admin;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.utils.DateUtil;
import lombok.Data;

import java.util.Date;

@Data
public class TransGroupInfoDTO {
    @JSONField(name = "TransGroupId")
    private String groupId;

    @JSONField(name = "TransGroupName")
    private String groupName;

    @JSONField(name = "CreateTime")
    private String createTime;

    public TransGroupInfoDTO(String groupId, String groupName, Date createTime) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.createTime = DateUtil.date2String(createTime);
    }

    public TransGroupInfoDTO(String groupId, String groupName, String createTime) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.createTime = createTime;
    }

}
