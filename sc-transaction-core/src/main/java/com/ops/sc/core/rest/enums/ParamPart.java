
package com.ops.sc.core.rest.enums;


public enum ParamPart {
    /**
     * Request path.
     */
    PATH,
    
    /**
     * Query parameters.
     */
    QUERY,
    
    /**
     * HTTP headers.
     */
    HEADER,
    
    /**
     * HTTP request body.
     */
    BODY,
    
    /**
     * Unknown source.
     */
    UNKNOWN,
}
