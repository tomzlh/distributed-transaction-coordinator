package com.ops.sc.ta.service;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.constant.RpcConstants;
import com.ops.sc.ta.anno.BranchOperation;
import com.ops.sc.ta.anno.BranchRecovery;
import com.ops.sc.ta.buid.BranchTransBeanBuilder;
import com.ops.sc.common.utils.RateLimiterService;
import com.ops.sc.core.service.tcc.TccExecuteInfo;
import com.ops.sc.common.utils.CommonUtils;
import com.ops.sc.ta.config.TaPropertyReader;
import com.ops.sc.ta.trans.TccTaCallIn;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


import java.lang.reflect.Method;

@Component
public class AnnotationProcessService implements BeanPostProcessor, InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationProcessService.class);

    // 事务分组ID
    private String transGroupId;

    // 应用名称(同spring.application.name)
    private String appName;

    // 事务消息模式
    private boolean localTransMessage;

    private String serverAddress;

    // 异步上报事务结果的线程池size
    private int asyncReportThreadPoolSize;

    // 默认false：开启事务框架; true：关闭事务框架
    private boolean serviceDisable;

    // 限流配置
    private String rateConfig;


    // 处理二阶段提交/回滚线程池大小
    private int rmClientPollThreadPoolSize;

    public AnnotationProcessService(String transGroupId, String serverAddress, String appName, boolean localTransMessage,
                                    int asyncReportThreadPoolSize, String rateConfig, boolean serviceDisable, int rmClientPollThreadPoolSize) {
        if(transGroupId==null){
            this.transGroupId = TaPropertyReader.getINSTANCE().getValue("sc.ta.transactionServiceGroup");
        }
        if(appName==null){
            this.appName = TaPropertyReader.getINSTANCE().getValue("sc.ta.applicationName");
        }
        this.transGroupId = transGroupId;
        this.serverAddress = serverAddress;
        this.appName = appName;
        this.localTransMessage = localTransMessage;
        this.asyncReportThreadPoolSize = asyncReportThreadPoolSize;
        this.rateConfig = rateConfig;
        this.serviceDisable = serviceDisable;
        this.rmClientPollThreadPoolSize = rmClientPollThreadPoolSize;
    }

    public AnnotationProcessService(String transGroupId, String serverAddress, String appName, boolean localTransMessage,
                                    int asyncReportThreadPoolSize, String rateConfig, boolean serviceDisable) {
        this(transGroupId, serverAddress, appName, localTransMessage, asyncReportThreadPoolSize, rateConfig,
                serviceDisable, RpcConstants.DEFAULT_TACLIENT_THREAD_POOL_SIZE);
    }

    public AnnotationProcessService(String transGroupId, String serverAddress, String appName, boolean localTransMessage) {
        this(transGroupId, serverAddress, appName, localTransMessage,RpcConstants.DEFAULT_TACLIENT_THREAD_POOL_SIZE,
                StringUtils.EMPTY, Boolean.FALSE, RpcConstants.DEFAULT_TACLIENT_THREAD_POOL_SIZE);
    }

    public AnnotationProcessService(String transGroupId, String serverAddress, String appName, boolean localTransMessage,
                                    boolean serviceDisable) {
        this(transGroupId, serverAddress, appName, localTransMessage, RpcConstants.DEFAULT_TACLIENT_THREAD_POOL_SIZE,
                StringUtils.EMPTY, serviceDisable, RpcConstants.DEFAULT_TACLIENT_THREAD_POOL_SIZE);
    }

    public AnnotationProcessService(String transGroupId, String serverAddress, String appName) {

        this(transGroupId, serverAddress, appName, Constants.MODE);
    }

    public AnnotationProcessService(String appName) {
        this(null, null, appName);
    }

    public AnnotationProcessService(){
        this(null,null,null);


    }

    @Override
    public void afterPropertiesSet() {
        if (serviceDisable) {
            LOGGER.info("Global transaction is disabled.");
            return;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Environment environment = applicationContext.getEnvironment();
        CommonUtils.initLoggingSystem(environment);
    }

    public boolean isServiceDisable() {
        return serviceDisable;
    }

    public void setServiceDisable(boolean serviceDisable) {
        this.serviceDisable = serviceDisable;
    }

    public boolean isLocalTransMessage() {
        return localTransMessage;
    }

    public void setLocalTransMessage(boolean localTransMessage) {
        this.localTransMessage = localTransMessage;
    }

    public String getTransGroupId() {
        return transGroupId;
    }

    public void setTransGroupId(String transGroupId) {
        this.transGroupId = transGroupId;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setRateConfig(String rateConfig) {
        this.rateConfig = rateConfig;
        RateLimiterService.modifyRateConfig(rateConfig);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        try {
            Class<?> beanClass = AopUtils.getTargetClass(bean);
            Method[] methods = beanClass.getDeclaredMethods();
            for (Method method : methods) {
                try {
                    BranchOperation branchOperation = AnnotationUtils.findAnnotation(method, BranchOperation.class);
                    BranchRecovery branchRecovery = AnnotationUtils.findAnnotation(method,
                            BranchRecovery.class);
                    if (branchOperation == null && branchRecovery == null) {
                        continue;
                    } else {
                        TccExecuteInfo tccExecuteInfo = new TccExecuteInfo();
                        tccExecuteInfo.setTccBeanName(beanName);
                        tccExecuteInfo.setTargetBean(bean);
                        String tagId = BranchTransBeanBuilder.getTagId(appName, method.getName());
                        tccExecuteInfo.setTagId(tagId);
                        if (branchOperation != null) {
                            tccExecuteInfo.setConfirmMethod(
                                    beanClass.getMethod(branchOperation.confirmMethod(), method.getParameterTypes()));
                            tccExecuteInfo.setCancelMethod(
                                    beanClass.getMethod(branchOperation.cancelMethod(), method.getParameterTypes()));
                        } else {
                            tccExecuteInfo.setCancelMethod(
                                    beanClass.getMethod(branchRecovery.recoveryMethod(), method.getParameterTypes()));
                        }
                        TccTaCallIn.getInstance().register(tccExecuteInfo);
                    }
                } catch (NoSuchMethodException | SecurityException e) {
                    LOGGER.error("Tcc callback method parse failed!", e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Bean post process error!", e);
        }
        return bean;
    }

    /**
     * 客户端资源统一释放
     * <p>
     */
    /*private void shutdown() {
        StateChecker.shutdown();
        RpcClientPool.getInstance().shutDown();
        if (CommonUtil.isLocalFrameMode()) {
            LocalResourceCallCallOut.shutdown();
        }
    }*/

}
