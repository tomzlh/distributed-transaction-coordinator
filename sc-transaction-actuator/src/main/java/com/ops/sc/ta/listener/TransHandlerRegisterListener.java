package com.ops.sc.ta.listener;

import com.ops.sc.core.spi.ClientServiceLoader;
import com.ops.sc.core.spi.TransHandlerSPI;
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

import java.lang.reflect.Modifier;
import java.util.Set;

@Component
public class TransHandlerRegisterListener implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransHandlerRegisterListener.class);


    @Override
    public void onApplicationEvent(ApplicationEvent event)  {
        try {
            if (event instanceof ContextRefreshedEvent) {
                LOGGER.info("scan transaction executors begin!");
                Reflections reflections = new Reflections(new ConfigurationBuilder()
                        .forPackages("com.ops.sc.ta") // 指定路径URL
                        .addScanners(new SubTypesScanner())
                        .addScanners(new MethodAnnotationsScanner()));
                Set<Class<? extends TransHandlerSPI>> transClasses = reflections.getSubTypesOf(TransHandlerSPI.class);
                if (transClasses != null && !transClasses.isEmpty()) {
                    for (Class clazz : transClasses) {
                        if(!Modifier.isAbstract(clazz.getModifiers())&&!clazz.isInterface()){
                            TransHandlerSPI transHandlerSPI = (TransHandlerSPI) clazz.newInstance();
                            ClientServiceLoader.registerHandlerInstance(transHandlerSPI.transactionName(), transHandlerSPI);
                        }
                    }
                }
                LOGGER.info("scan transaction executors end!");
            }
        }catch (Exception e){
            LOGGER.info("scan transaction executors error!",e);
        }
    }


}
