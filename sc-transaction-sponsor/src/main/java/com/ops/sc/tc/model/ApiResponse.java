package com.ops.sc.tc.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@Builder
@ToString
public class ApiResponse implements Serializable {

    private static final long serialVersionUID = 9061424060444688372L;

    @JSONField(name = "Message")
    private String message;

    @JSONField(name = "Code")
    private String code;

    @JSONField(name = "BusinessId")
    private String businessId;

}
