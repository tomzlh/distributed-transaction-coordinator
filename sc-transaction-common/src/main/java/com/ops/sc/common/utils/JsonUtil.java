package com.ops.sc.common.utils;

import java.util.Collections;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;


public final class JsonUtil {

    /**
     * 将对象转变为json字符串
     *
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return StringUtils.EMPTY;
        }
        return JSON.toJSONString(obj);
    }

    public static String toString(Object obj, SerializerFeature feature) {
        if (obj == null) {
            return StringUtils.EMPTY;
        }
        return JSON.toJSONString(obj, feature);
    }

    /**
     * json字符串转变成map对象
     *
     * @param json
     *            json字符串
     * @return
     */
    public static <T> Map<String, T> toMap(String json) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyMap();
        }
        return JSON.parseObject(json, new TypeReference<Map<String, T>>() {
        });
    }

    /**
     * json字符串转变为特定对象
     *
     * @param clazz
     *            对象
     * @param json
     *            字符串
     * @return
     */
    public static <T> T toObject(Class<T> clazz, String json) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        if (clazz.isAssignableFrom(String.class)) {
            return (T) json;
        }
        return JSON.parseObject(json, clazz);
    }
}
