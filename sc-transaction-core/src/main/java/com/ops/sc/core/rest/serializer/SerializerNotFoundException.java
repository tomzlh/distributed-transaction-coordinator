
package com.ops.sc.core.rest.serializer;

import java.text.MessageFormat;

/**
 * {@link ResponseBodySerializer} not found for specific MIME type.
 */
public final class SerializerNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 3201288074956273247L;
    
    public SerializerNotFoundException(final String mimeType) {
        super(MessageFormat.format("ResponseBodySerializer not found for [{0}]", mimeType));
    }
}
