package com.ops.sc.tc.anno;

import com.ops.sc.common.enums.CallbackStrategy;
import com.ops.sc.common.enums.TimeoutType;
import com.ops.sc.common.enums.TransIsolation;
import com.ops.sc.common.enums.TransMode;
import java.lang.annotation.*;
import static com.ops.sc.common.constant.Constants.DEFAULT_TIMEOUT;
import static com.ops.sc.common.enums.CallbackStrategy.OUT_OF_ORDER;
import static com.ops.sc.common.enums.TimeoutType.CANCEL;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DistributeTrans {

    String name() default "";

    long timeout() default DEFAULT_TIMEOUT;

    CallbackStrategy callbackStrategy() default OUT_OF_ORDER;

    TimeoutType timeoutType() default CANCEL;

    TransMode transMode() default TransMode.XA;

    String groupId() default "";

    String bizId() default "";

    String bizParam() default "";
}
