package com.ops.sc.ta.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.ops.sc.common","com.ops.sc.core.rest","com.ops.sc.ta" })
public class TaMainConfig {

}
