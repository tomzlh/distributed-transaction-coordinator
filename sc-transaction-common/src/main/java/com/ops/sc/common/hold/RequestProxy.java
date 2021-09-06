package com.ops.sc.common.hold;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.HttpMethod;
import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class RequestProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestProxy.class);

    private static final Long CONNECTION_TIMEOUT = Constants.CONNECTION_TIMEOUT / 1000;
    private static final Long READ_TIMEOUT = Constants.DEFAULT_TIMEOUT / 2 / 1000;
    private static final Long WRITE_TIMEOUT = Constants.DEFAULT_TIMEOUT / 2 / 1000;

    private static final Long KEEPALIVE_DURATION = 10L;
    private static final Integer MAX_IDLE_CONNECTION_COUNT = 10; // 最大空闲连接数
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json;charset=UTF-8");

    private static final String HTTP_HEADER_CONNECTION = "Connection";
    private static final String HTTP_HEADER_KEEP_ALIVE = "keep-alive";

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS).readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(MAX_IDLE_CONNECTION_COUNT, KEEPALIVE_DURATION, TimeUnit.SECONDS))
            .retryOnConnectionFailure(true).build();

    /**
     * 封装GET请求
     *
     * @param url
     * @param queryParam
     * @param headParam
     * @return
     */
    public static ResponseStatus get(String url, Map<String, String> queryParam, Map<String, String> headParam)
            throws IOException {
        return directCall(url, queryParam, headParam, null, HttpMethod.GET);
    }

    /**
     * 封装GET请求
     *
     * @param url
     * @param headParam
     * @return
     */
    public static ResponseStatus get(String url, Map<String, String> headParam) throws IOException {
        return directCall(url, null, headParam, null, HttpMethod.GET);
    }

    /**
     * 封装GET请求
     *
     * @param url
     * @return
     */
    public static ResponseStatus get(String url) throws IOException {
        return directCall(url, null, null, null, HttpMethod.GET);
    }

    /**
     * 封装POST请求，请求体必须是JSON
     *
     * @param url
     * @param queryParam
     * @param headParam
     * @param requestBody
     * @return
     */
    public static ResponseStatus post(String url, Map<String, String> queryParam, Map<String, String> headParam,
                                      String requestBody) throws IOException {
        return directCall(url, queryParam, headParam, requestBody, HttpMethod.POST);
    }

    public static ResponseStatus post(String url, Map<String, String> headParam, String requestBody)
            throws IOException {
        return directCall(url, null, headParam, requestBody, HttpMethod.POST);
    }

    public static ResponseStatus post(String url, String requestBody) throws IOException {
        return directCall(url, null, null, requestBody, HttpMethod.POST);
    }

    /**
     * 封装PUT请求，请求体必须是JSON
     *
     * @param url
     * @param queryParam
     * @param headParam
     * @param requestBody
     * @return
     */
    public static ResponseStatus put(String url, Map<String, String> queryParam, Map<String, String> headParam,
                                     String requestBody) throws IOException {
        return directCall(url, queryParam, headParam, requestBody, HttpMethod.PUT);
    }

    /**
     * 封装DELETE请求，不能包含请求体
     *
     * @param url
     * @param queryParam
     * @param headParam
     * @return
     */
    public static ResponseStatus delete(String url, Map<String, String> queryParam, Map<String, String> headParam)
            throws IOException {
        return directCall(url, queryParam, headParam, null, HttpMethod.DELETE);
    }

    /**
     * 封装请求
     *
     * @param url
     * @param queryParam
     * @param headParam
     * @param requestBody
     * @param method
     * @return
     */
    public static ResponseStatus directCall(String url, Map<String, String> queryParam, Map<String, String> headParam,
                                            String requestBody, HttpMethod method) throws IOException {

        if (queryParam != null && queryParam.size() > 0) {
            url = url + "?" + Joiner.on('&').withKeyValueSeparator('=').join(queryParam);
        }

        if (headParam == null) {
            headParam = Maps.newHashMap();
        }

        headParam.put(HTTP_HEADER_CONNECTION, HTTP_HEADER_KEEP_ALIVE);

        LOGGER.info("Request Start url:{},headParam:{},method:{},requestBody:{}", url, headParam, method, requestBody);

        Request request;

        switch (method) {
        case GET:
            request = new Request.Builder().url(url).headers(Headers.of(headParam)).build();
            break;
        case POST:
            request = new Request.Builder().url(url)
                    .post(RequestBody.create(JSON_MEDIA,
                            requestBody == null ? new JSONObject().toString() : requestBody))
                    .headers(Headers.of(headParam)).build();
            break;
        case PUT:
            request = new Request.Builder().url(url)
                    .put(RequestBody.create(JSON_MEDIA,
                            requestBody == null ? new JSONObject().toString() : requestBody))
                    .headers(Headers.of(headParam)).build();
            break;
        case DELETE:
            request = new Request.Builder().url(url).delete().headers(Headers.of(headParam)).build();
            break;
        default:
            throw new IllegalStateException("not support this Http Method");
        }

        try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            String responseBody = response.body() != null ? Objects.toString(response.body().string()) : "";
            LOGGER.info("Request End Response:{}", responseBody);
            if (response.isSuccessful()) {
                return ResponseStatus.successOf(responseBody);
            } else {
                return ResponseStatus.failOf(response.code(), responseBody);
            }
        }
    }

}