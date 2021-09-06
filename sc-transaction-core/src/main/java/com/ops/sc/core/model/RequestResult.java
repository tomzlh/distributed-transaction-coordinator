package com.ops.sc.core.model;

import com.google.common.collect.Maps;

import java.util.Map;


public class RequestResult {

    private RequestStatus status;
    private Map<String, Object> attachments;

    private String errorMsg;

    public RequestResult(RequestStatus status) {
        this(status, Maps.newHashMap());
    }

    public RequestResult(RequestStatus status, Map<String, Object> attachments) {
        this.status = status;
        this.attachments = attachments;
    }

    public static RequestResult ok() {
        return new RequestResult(RequestStatus.SUCCESS);
    }

    public static RequestResult fail() {
        return new RequestResult(RequestStatus.FAILED);
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public RequestResult setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public RequestResult addAttachment(String key, String value) {
        this.attachments.put(key, value);
        return this;
    }

    public Object getAttachment(String key) {
        return this.attachments.get(key);
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public boolean isSuccess() {
        return status == RequestStatus.SUCCESS;
    }

    public boolean isFail() {
        return status == RequestStatus.FAILED;
    }

    @Override
    public String toString() {
        return "SendResult{" + "status=" + status + ", attachments=" + attachments + '}';
    }
}
