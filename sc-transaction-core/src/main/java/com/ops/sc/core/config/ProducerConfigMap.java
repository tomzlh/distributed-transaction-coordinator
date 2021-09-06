package com.ops.sc.core.config;

import com.ops.sc.common.utils.JsonUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class ProducerConfigMap implements Serializable {
    private static final long serialVersionUID = 7345865779539365954L;

    private Map<String, Object> configMap = new HashMap<>();

    public ProducerConfigMap() {
    }

    public ProducerConfigMap(Map<String, Object> configMap) {
        this.configMap = new HashMap<>(configMap);
    }

    public static ProducerConfigMap fromJsonStr(String configStr) {
        Map<String, Object> configMap = JsonUtil.toMap(configStr);
        return new ProducerConfigMap(configMap);
    }

    public ProducerConfigMap addConfig(String key, Object value) {
        configMap.put(key, value);
        return this;
    }

    public Map<String, Object> getAllConfigs() {
        return configMap;
    }

    public Object getConfig(String key) {
        return configMap.get(key);
    }

    public String toJsonStr() {
        return JsonUtil.toString(this);
    }

    @Override
    public String toString() {
        return "ProducerConfig{" + "configMap=" + configMap + '}';
    }
}
