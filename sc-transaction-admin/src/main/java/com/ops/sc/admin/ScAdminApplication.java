package com.ops.sc.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "com.ops.sc.common","com.ops.sc.mybatis.datasource","com.ops.sc.core","com.ops.sc.admin" })
@EnableScheduling
@ImportResource("classpath:applicationContext.xml")
@EnableAsync
@MapperScan(basePackages = "com.ops.sc.mybatis.mapper")
public class ScAdminApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScAdminApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ScAdminApplication.class, args);
    }

}
