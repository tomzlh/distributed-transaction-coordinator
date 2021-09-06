package com.ops.sc.compensator;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = { "com.ops.sc.common","com.ops.sc.mybatis.datasource","com.ops.sc.core","com.ops.sc.compensator" })
@EnableScheduling
@ImportResource("classpath:applicationContext.xml")
@EnableAsync
@MapperScan(basePackages = "com.ops.sc.mybatis.mapper")
public class ScCompensatorApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScCompensatorApplication.class);


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ScCompensatorApplication.class, args);
        Environment environment = context.getBean(Environment.class);
        LOGGER.info("SC Compensator started!");
    }

}
