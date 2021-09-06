package com.ops.sc.ta.anno;

import com.ops.sc.ta.listener.BranchRegisterListener;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Import(value = {BranchRegisterListener.class
})
public @interface EnableTaClient {
}
