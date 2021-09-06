
package com.ops.sc.core.rest.deserializer.impl;

import com.google.gson.Gson;
import com.ops.sc.common.utils.GsonUtil;
import com.ops.sc.core.rest.deserializer.RequestBodyDeserializer;
import io.netty.handler.codec.http.HttpHeaderValues;


import java.nio.charset.StandardCharsets;


public final class JsonRequestBodyDeserializer implements RequestBodyDeserializer {
    
    private final Gson gson = GsonUtil.getGson();
    
    @Override
    public String mimeType() {
        return HttpHeaderValues.APPLICATION_JSON.toString();
    }
    
    @Override
    public <T> T deserialize(final Class<T> targetType, final byte[] requestBodyBytes) {
        if (0 == requestBodyBytes.length) {
            return null;
        }
        return gson.fromJson(new String(requestBodyBytes, StandardCharsets.UTF_8), targetType);
    }
}
