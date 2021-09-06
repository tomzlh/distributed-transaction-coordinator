
package com.ops.sc.core.rest.serializer.factory.impl;

import com.ops.sc.core.rest.serializer.ResponseBodySerializer;
import com.ops.sc.core.rest.serializer.factory.SerializerFactory;
import com.ops.sc.core.rest.serializer.impl.JsonResponseBodySerializer;
import io.netty.handler.codec.http.HttpHeaderValues;



public final class JsonResponseBodySerializerFactory implements SerializerFactory {
    
    @Override
    public String mimeType() {
        return HttpHeaderValues.APPLICATION_JSON.toString();
    }
    
    @Override
    public ResponseBodySerializer createSerializer() {
        return new JsonResponseBodySerializer();
    }
}
