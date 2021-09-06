
package com.ops.sc.core.rest.annotation;

import com.ops.sc.core.rest.enums.ParamPart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Parameter {
    
    /**
     * Parameter name.
     *
     * @return Parameter name
     */
    String name();
    
    /**
     * Source of parameter.
     *
     * @return Source of parameter
     */
    ParamPart source();
    
    /**
     * If the parameter is required.
     *
     * @return Requirement
     */
    boolean required() default true;
}
