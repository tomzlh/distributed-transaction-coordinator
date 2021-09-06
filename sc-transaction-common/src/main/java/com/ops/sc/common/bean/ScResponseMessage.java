package com.ops.sc.common.bean;

import com.ops.sc.common.enums.MessageType;
import com.ops.sc.common.enums.TransRecordType;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
public class ScResponseMessage {

    protected ResultInfo resultInfo;

    protected String msg;


    protected String businessId;

    protected long tid;

    /**
     * The Branch id.
     */
    protected long branchId;

    private Map<String,String> returnParamMap;
    /**
     * The Branch status.
     */
    protected int messageType;


    public static class ResultInfo{
        public String message;
        public String code;
    }
}
