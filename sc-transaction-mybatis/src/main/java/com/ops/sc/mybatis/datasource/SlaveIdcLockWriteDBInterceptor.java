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
public class SlaveIdcLockWriteDBInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlaveIdcLockWriteDBInterceptor.class);

    public SlaveIdcLockWriteDBInterceptor() {
        LOGGER.info("LockWriteDataSourceInterceptor enable");
    }

    @Pointcut("execution(* com.ops.sc.core.lock.db.DBLockStore.batchInsert(..)) || execution(* com.ops.sc.core.lock.db.DBLockStore.deleteByTidAndBranchId(..))")
    public void aspectPoint() {
    }

    @Before("aspectPoint()")
    public void advice(JoinPoint jp) {
        LOGGER.debug("Switch to LockWriteDB before {}", jp.getSignature().getName());
        MultipleDataSource.setDataSourceKey(DataSourceInstance.LockWrite);
    }

    @After("aspectPoint()")
    public void adviceAfterReturn(JoinPoint jp) {
        LOGGER.debug("Switch to MetaDataDB after {}", jp.getSignature().getName());
        MultipleDataSource.setDataSourceKey(DataSourceInstance.MetaData);
    }

}
