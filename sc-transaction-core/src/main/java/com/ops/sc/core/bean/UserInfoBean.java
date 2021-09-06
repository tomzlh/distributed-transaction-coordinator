package com.ops.sc.core.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserInfoBean {

    @JSONField(name = "AccountId")
    private String accountId;

    @JSONField(name = "UserName")
    private String userName;

    @JSONField(name = "AccessKeyId")
    private String accessKeyId;

}
