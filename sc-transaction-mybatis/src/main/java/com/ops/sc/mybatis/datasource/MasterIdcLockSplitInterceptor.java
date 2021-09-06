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
@Aspect
@ConditionalOnExpression("!${sc.server.deploy.idc.role.slave:false} && ${sc.server.db.meta.lock.split:false}")
public class MasterIdcLockSplitInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterIdcLockSplitInterceptor.class);

    public MasterIdcLockSplitInterceptor() {
        LOGGER.info("MasterIdcLockSplitInterceptor Enable");
    }


    @Pointcut("execution(* com.ops.sc.core.lock.db.DBLockStore.*(..))")
    public void lockAspectPoint() {
    }

    @Before("lockAspectPoint()")
    public void advice(JoinPoint jp) {
        LOGGER.debug("Switch to LockDB before {}", jp.getSignature().getName());
        MultipleDataSource.setDataSourceKey(DataSourceInstance.LockWrite);
    }

    @After("lockAspectPoint()")
    public void adviceAfterReturn(JoinPoint jp) {
        LOGGER.debug("Switch to MetaDB after {}", jp.getSignature().getName());
        MultipleDataSource.setDataSourceKey(DataSourceInstance.MetaData);
    }

}
