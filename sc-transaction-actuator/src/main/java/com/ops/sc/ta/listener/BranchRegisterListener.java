package com.ops.sc.ta.listener;

import com.ops.sc.common.bean.TransRegisterRequest;
import com.ops.sc.ta.config.TaConfiguration;
import com.ops.sc.core.spi.ClientServiceLoader;
import com.ops.sc.core.spi.TransHandlerSPI;
import com.ops.sc.ta.anno.TccBranchTransaction;
import com.ops.sc.ta.anno.XaBranchTransaction;
import com.ops.sc.ta.config.TaPropertyReader;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BranchRegisterListener implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchRegisterListener.class);


    private String scanBasicPackage;

    private static final AtomicBoolean START = new AtomicBoolean(false);



    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        /*if (event instanceof ContextRefreshedEvent) {
            if (START.compareAndSet(false, true)) {
                try {
                    TaNettyClient taNettyClient = initNettyClient();
                    branchDetector(taNettyClient);
                    LOGGER.info("Register branch transactions SUCCEED!");
                } catch (Exception e) {
                    LOGGER.error("Register branch transactions FAILED!",e);
                }
            } else {
                LOGGER.info("Register branch transactions have been finished!");
            }
        }*/

    }

    /*private TaNettyClient initNettyClient(){
        TaNettyClient taNettyClient = TaNettyClient.getInstance(TaConfiguration.instance.getApplicationName(), TaConfiguration.instance.getTransactionServiceGroup(),TaConfiguration.instance.getRegisterType());
        taNettyClient.init();
        return taNettyClient;
    }


    private void branchDetector(TaNettyClient taNettyClient) throws InstantiationException, IllegalAccessException{
        scanBasicPackage = getScanBasicPackage();
        ConfigurationBuilder builder = new ConfigurationBuilder().useParallelExecutor();
        if (StringUtils.isEmpty(scanBasicPackage)) {
            builder.setUrls(ClasspathHelper.forClassLoader());
        } else {
            builder.setUrls(ClasspathHelper.forPackage(scanBasicPackage));
        }
        builder.setScanners(new MethodAnnotationsScanner(), new TypeAnnotationsScanner());

        Reflections reflections = new Reflections(builder);
        Set<Class<?>> tccClasses = reflections.getTypesAnnotatedWith(TccBranchTransaction.class);
        if(tccClasses!=null&&!tccClasses.isEmpty()){
            for (Class clazz : tccClasses) {
                TccBranchTransaction tccBranchTransaction = (TccBranchTransaction) clazz.getAnnotation(TccBranchTransaction.class);
                String transactionName = tccBranchTransaction.transactionName();
                ClientServiceLoader.registerHandlerInstance(transactionName,
                        (TransHandlerSPI) clazz.newInstance());
                taNettyClient.registerResource(TransRegisterRequest.builder().applicationName(TaConfiguration.instance.getApplicationName()).transactionServiceGroup(TaConfiguration.instance.getTransactionServiceGroup()).build());
            }
        }
        Set<Class<?>> xaClasses = reflections.getTypesAnnotatedWith(XaBranchTransaction.class);
        if(xaClasses!=null&&!xaClasses.isEmpty()){
            for (Class clazz : xaClasses) {
                XaBranchTransaction tccBranchTransaction = (XaBranchTransaction) clazz.getAnnotation(XaBranchTransaction.class);
                String transactionName = tccBranchTransaction.transactionName();
                ClientServiceLoader.registerHandlerInstance(transactionName,(TransHandlerSPI) clazz.newInstance());
                taNettyClient.registerResource(TransRegisterRequest.builder().applicationName(TaConfiguration.instance.getApplicationName()).transactionServiceGroup(TaConfiguration.instance.getTransactionServiceGroup()).dataSource(TaConfiguration.instance.getDataSource()).build());
            }
        }

    }

    private String getScanBasicPackage() {
        return TaPropertyReader.getINSTANCE().getValue("sc.ta.scanPackage");
    }*/

}
