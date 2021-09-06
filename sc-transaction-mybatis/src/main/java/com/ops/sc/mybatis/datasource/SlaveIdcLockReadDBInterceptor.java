package com.ops.sc.mybatis.datasource;

import com.ops.sc.common.enums.DataSourceInstance;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("${sc.server.deploy.idc.role.slave:false}")
@Aspect
public class SlaveIdcLockReadDBInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlaveIdcLockReadDBInterceptor.class);

    public SlaveIdcLockReadDBInterceptor() {
        LOGGER.info("LockReadDataSourceInterceptor enable");
    }

    @Pointcut("execution(* com.ops.sc.core.lock.db.DBLockStore.queryTransLockList(..))")
    public void lockReadAspectPoint() {
    }

    @Before("lockReadAspectPoint()")
    public void advice(JoinPoint jp) {
        LOGGER.debug("Switch to LockReadDB before {}", jp.getSignature().getName());
        MultipleDataSource.setDataSourceKey(DataSourceInstance.LockRead);
    }

    @After("lockReadAspectPoint()")
    public void adviceAfterReturn(JoinPoint jp) {
        LOGGER.debug("Switch to MetaDataDB after {}", jp.getSignature().getName());
        MultipleDataSource.setDataSourceKey(DataSourceInstance.MetaData);
    }

}
