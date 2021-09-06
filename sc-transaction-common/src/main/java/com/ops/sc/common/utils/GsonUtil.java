package com.ops.sc.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;


public class GsonUtil {

    private static Gson gson = null;
    static {
            gson= new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    private static final GsonBuilder GSON_BUILDER = new GsonBuilder();


    private static final JsonParser JSON_PARSER = new JsonParser();

    /**
     * Object 转成 json
     *
     * @param src
     * @return String
     */
    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    /**
     * json 转成 特定的cls的Object
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    /**
     * json 转成 特定的 rawClass<classOfT> 的Object
     *
     * @param json
     * @param classOfT
     * @param argClassOfT
     * @return
     */
    public static <T> T fromJson(String json, Class<T> classOfT, Class argClassOfT) {
        Type type = new ParameterizedType4ReturnT(classOfT, new Class[]{argClassOfT});
        return gson.fromJson(json, type);
    }
    public static class ParameterizedType4ReturnT implements ParameterizedType {
        private final Class raw;
        private final Type[] args;
        public ParameterizedType4ReturnT(Class raw, Type[] args) {
            this.raw = raw;
            this.args = args != null ? args : new Type[0];
        }
        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }
        @Override
        public Type getRawType() {
            return raw;
        }
        @Override
        public Type getOwnerType() {return null;}
    }

    /**
     * json 转成 特定的cls的list
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> List<T> fromJsonList(String json, Class<T> classOfT) {
        return gson.fromJson(
                json,
                new TypeToken<List<T>>() {
                }.getType()
        );
    }

    /**
     * Register type adapter.
     *
     * @param type Gson type
     * @param typeAdapter Gson type adapter
     */
    public static synchronized void registerTypeAdapter(final Type type, final TypeAdapter typeAdapter) {
        GSON_BUILDER.registerTypeAdapter(type, typeAdapter);
        gson = GSON_BUILDER.create();
    }

    /**
     * Get gson instance.
     *
     * @return gson instance
     */
    public static Gson getGson() {
        return gson;
    }

    /**
     * Get json parser.
     *
     * @return json parser instance
     */
    public static JsonParser getJsonParser() {
        return JSON_PARSER;
    }

}
