
package com.ops.sc.core.rest.serializer.factory;


import com.ops.sc.core.rest.serializer.ResponseBodySerializer;

public interface SerializerFactory {
    
    /**
     * Specify which type would be serialized by the serializer created by this factory.
     *
     * @return MIME type
     */
    String mimeType();
    
    /**
     * Serializer factory method.
     *
     * @return instance of serializer
     */
    ResponseBodySerializer createSerializer();
}
