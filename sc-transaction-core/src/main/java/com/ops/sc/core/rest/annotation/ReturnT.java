
package com.ops.sc.core.rest.annotation;


import com.ops.sc.core.rest.Http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate on handler method to declare HTTP status code and content type of HTTP response.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReturnT {
    
    /**
     * HTTP status code to return after handling.
     *
     * @return Http status code
     */
    int code() default 200;
    
    /**
     * HTTP content type of response.
     *
     * @return HTTP content type
     */
    String contentType() default Http.DEFAULT_CONTENT_TYPE;
}
