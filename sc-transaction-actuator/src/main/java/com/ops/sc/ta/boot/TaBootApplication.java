package com.ops.sc.ta.boot;

import com.ops.sc.ta.config.TaMainConfig;
import com.ops.sc.ta.service.TransActuatorInitService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@EnableAutoConfiguration
@Import(TaMainConfig.class)
@SpringBootApplication
public class TaBootApplication {

     public static void main(String[] args){
          ConfigurableApplicationContext context = SpringApplication.run(TaBootApplication.class, args);
          TransActuatorInitService transActuatorInitService = context.getBean(TransActuatorInitService.class);
          transActuatorInitService.start();

          Runtime.getRuntime().addShutdownHook(new Thread(() -> {
             System.err.println("Shutting down ta boot application");
             transActuatorInitService.close();
         }));
     }
}
