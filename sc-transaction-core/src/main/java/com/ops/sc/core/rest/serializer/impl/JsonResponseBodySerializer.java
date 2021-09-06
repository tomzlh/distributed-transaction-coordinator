
package com.ops.sc.core.rest.serializer.impl;

import com.google.gson.Gson;
import com.ops.sc.common.utils.GsonUtil;
import com.ops.sc.core.rest.serializer.ResponseBodySerializer;
import io.netty.handler.codec.http.HttpHeaderValues;


import java.nio.charset.StandardCharsets;

public final class JsonResponseBodySerializer implements ResponseBodySerializer {
    
    private final Gson gson = GsonUtil.getGson();
    
    @Override
    public String mimeType() {
        return HttpHeaderValues.APPLICATION_JSON.toString();
    }
    
    @Override
    public byte[] serialize(final Object responseBody) {
        if (responseBody instanceof String) {
            return ((String) responseBody).getBytes(StandardCharsets.UTF_8);
        }
        return gson.toJson(responseBody).getBytes(StandardCharsets.UTF_8);
    }
}
