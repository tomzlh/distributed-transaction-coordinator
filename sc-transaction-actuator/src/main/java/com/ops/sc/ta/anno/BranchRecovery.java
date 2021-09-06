package com.ops.sc.ta.anno;


import com.ops.sc.common.enums.TimeoutType;

import java.lang.annotation.*;

import static com.ops.sc.common.constant.Constants.DEFAULT_TIMEOUT;
import static com.ops.sc.common.enums.TimeoutType.ALARM;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface BranchRecovery {

    String name() default "";

    String recoveryMethod() default "";

    long timeout() default DEFAULT_TIMEOUT;

    TimeoutType timeoutType() default ALARM;

}
