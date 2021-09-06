
package com.ops.sc.core.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RootContext {
    
    /**
     * Context path.
     * Starts with '/' and no '/' at the end.
     * Such as <code>/api/app</code>.
     *
     * @return Context path
     */
    String value();
}
