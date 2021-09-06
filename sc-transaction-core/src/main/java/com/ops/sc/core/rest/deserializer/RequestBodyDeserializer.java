
package com.ops.sc.core.rest.deserializer;

public interface RequestBodyDeserializer {
    
    /**
     * Specify which type would be deserialized by this deserializer.
     *
     * @return MIME type
     */
    String mimeType();
    
    /**
     * Deserialize request body to an object.
     *
     * @param targetType       Target type
     * @param requestBodyBytes Request body bytes
     * @param <T>              Target type
     * @return Deserialized object
     */
    <T> T deserialize(Class<T> targetType, byte[] requestBodyBytes);
}
