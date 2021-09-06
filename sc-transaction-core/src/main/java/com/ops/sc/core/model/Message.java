package com.ops.sc.core.model;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;


public class Message implements Serializable {

    private static final long serialVersionUID = -2773536475174704866L;
    // 消息体
    private String payload;

    // 消息特征值
    private Map<String, Object> traits;

    public Message(String payload) {
        this(payload, Maps.newHashMap());
    }

    public Message(String payload, Map<String, Object> traits) {
        this.payload = payload;
        this.traits = traits;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, Object> addTrait(String key, Object value) {
        traits.put(key, value);
        return traits;
    }

    public Object getTrait(String key) {
        return traits.get(key);
    }

    public Map<String, Object> getTraits() {
        return traits;
    }

    @Override
    public String toString() {
        return "Message{" + "payload='" + payload + '\'' + ", traits=" + traits + '}';
    }
}
