package com.ops.sc.core.rest;

import com.ops.sc.core.rest.annotation.ScController;
import com.ops.sc.core.rest.config.RpcServiceConfiguration;
import com.ops.sc.core.rest.mapping.MappingRegistry;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class RestControllerListener implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestControllerListener.class);


    @Override
    public void onApplicationEvent(ApplicationEvent event)  {
        try {
            if (event instanceof ContextRefreshedEvent) {
                LOGGER.info("scan transaction executors begin!");
                ConfigurationBuilder builder = new ConfigurationBuilder().useParallelExecutor();
                builder.setUrls(ClasspathHelper.forPackage("com.ops.sc"));
                builder.setScanners(new MethodAnnotationsScanner(), new TypeAnnotationsScanner(), new SubTypesScanner());
                Reflections reflections = new Reflections(builder);
                Set<Class<?>> tccClasses = reflections.getTypesAnnotatedWith(ScController.class);
                if(tccClasses!=null&&!tccClasses.isEmpty()){
                    List<Object> instanceList=new ArrayList<>();
                    for (Class clazz : tccClasses) {
                        instanceList.add(clazz.newInstance());
                    }
                    MappingRegistry.initMappingRegistry(instanceList);
                    RpcServiceConfiguration.addControllerInstance(instanceList);
                }
                LOGGER.info("scan transaction executors end!");
            }
        }catch (Exception e){
            LOGGER.info("scan transaction executors error!",e);
        }
    }



}
