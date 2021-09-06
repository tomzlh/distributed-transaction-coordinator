package com.ops.sc.ta.anno;

import com.ops.sc.common.enums.TransMode;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface TccBranchTransaction {

    String transactionName() default "";

    TransMode transMode() default TransMode.TCC;

}
