
package com.ops.sc.core.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare what HTTP method and path is used to invoke the handler.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapPath {
    
    /**
     * Http method.
     *
     * @return Http method
     */
    String method();
    
    /**
     * Path pattern of this handler. Starts with '/'.
     * Such as <code>/app/{jobName}/enable</code>.
     *
     * @return Path pattern
     */
    String path() default "";
}
