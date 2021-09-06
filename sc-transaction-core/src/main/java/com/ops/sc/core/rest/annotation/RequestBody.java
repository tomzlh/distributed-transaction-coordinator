
package com.ops.sc.core.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate the parameter which is from HTTP request body.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestBody {
    
    /**
     * If request body is required.
     *
     * @return Required
     */
    boolean required() default true;
}
