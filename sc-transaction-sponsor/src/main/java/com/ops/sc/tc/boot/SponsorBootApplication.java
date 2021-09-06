package com.ops.sc.tc.boot;

import com.ops.sc.core.rest.NettyServerFacade;
import com.ops.sc.core.rest.RestControllerListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@MapperScan(basePackages = "com.ops.sc.mybatis.mapper")
@ComponentScan({"com.ops.sc.common","com.ops.sc.mybatis.datasource","com.ops.sc.core","com.ops.sc.tc" })
@SpringBootApplication
public class SponsorBootApplication {

       @Autowired
       RestControllerListener restControllerListener;

       public static void main(String[] args){
              ConfigurableApplicationContext context = SpringApplication.run(SponsorBootApplication.class, args);
              NettyServerFacade.getInstance().startup();
              shutdownHook();
       }

       private static void shutdownHook(){
              Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                     System.err.println("Shutting down sponsor!");
                     NettyServerFacade.getInstance().shutdown();
              }));
       }
}
