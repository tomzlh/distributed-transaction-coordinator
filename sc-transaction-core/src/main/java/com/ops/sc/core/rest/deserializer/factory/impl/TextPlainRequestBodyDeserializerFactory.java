

package com.ops.sc.core.rest.deserializer.factory.impl;

import com.ops.sc.core.rest.deserializer.RequestBodyDeserializer;
import com.ops.sc.core.rest.deserializer.factory.DeserializerFactory;
import com.ops.sc.core.rest.deserializer.impl.TextPlainRequestBodyDeserializer;
import io.netty.handler.codec.http.HttpHeaderValues;


public final class TextPlainRequestBodyDeserializerFactory implements DeserializerFactory {
    
    @Override
    public String mimeType() {
        return HttpHeaderValues.TEXT_PLAIN.toString();
    }
    
    @Override
    public RequestBodyDeserializer createDeserializer() {
        return new TextPlainRequestBodyDeserializer();
    }
}
