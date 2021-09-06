
package com.ops.sc.core.rest.deserializer.factory.impl;

import com.ops.sc.core.rest.deserializer.RequestBodyDeserializer;
import com.ops.sc.core.rest.deserializer.factory.DeserializerFactory;
import com.ops.sc.core.rest.deserializer.impl.JsonRequestBodyDeserializer;
import io.netty.handler.codec.http.HttpHeaderValues;


public final class JsonRequestBodyDeserializerFactory implements DeserializerFactory {
    
    @Override
    public String mimeType() {
        return HttpHeaderValues.APPLICATION_JSON.toString();
    }
    
    @Override
    public RequestBodyDeserializer createDeserializer() {
        return new JsonRequestBodyDeserializer();
    }
}
