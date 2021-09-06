
package com.ops.sc.core.rest.deserializer.impl;

import com.ops.sc.core.rest.deserializer.RequestBodyDeserializer;
import io.netty.handler.codec.http.HttpHeaderValues;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

/**
 * Default deserializer for <code>text/plain</code>.
 */
public final class TextPlainRequestBodyDeserializer implements RequestBodyDeserializer {
    
    @Override
    public String mimeType() {
        return HttpHeaderValues.TEXT_PLAIN.toString();
    }
    
    @Override
    public <T> T deserialize(final Class<T> targetType, final byte[] requestBodyBytes) {
        if (byte[].class.equals(targetType)) {
            return (T) requestBodyBytes;
        }
        if (String.class.isAssignableFrom(targetType)) {
            return (T) new String(requestBodyBytes, StandardCharsets.UTF_8);
        }
        throw new UnsupportedOperationException(MessageFormat.format("Cannot deserialize [{0}] into [{1}]", mimeType(), targetType.getName()));
    }
}
