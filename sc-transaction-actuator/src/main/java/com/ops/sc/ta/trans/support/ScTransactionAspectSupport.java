package com.ops.sc.ta.trans.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import java.lang.reflect.Method;

@Component("scTransactionAspectSupport")
public class ScTransactionAspectSupport extends TransactionAspectSupport implements Ordered, InitializingBean {

    private static final String TRANS_ATTRIBUTE_NAME = "scProvidedTransactionManager";
    @Autowired(required = false)
    private ScTransactionManager scTransactionManager;

    public Object runInTransaction(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        try {
            ScProvidedTransactionManagerRecorder.setInProvidedTM(true);
            return invokeWithinTransaction(methodSignature.getMethod(), pjp.getTarget().getClass(), pjp::proceed);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            ScProvidedTransactionManagerRecorder.setInProvidedTM(false);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void afterPropertiesSet() {
        super.setTransactionManager(scTransactionManager);
        TransactionAttributeSource tas = (Method method, Class<?> aClass) -> new ScTransactionAttribute();

        super.setTransactionAttributeSource(tas);
        super.afterPropertiesSet();
    }



    static class ScTransactionAttribute implements TransactionAttribute {
        @Override
        public String getQualifier() {
            return null;
        }

        @Override
        public boolean rollbackOn(Throwable throwable) {
            return true;
        }

        @Override
        public int getPropagationBehavior() {
            return PROPAGATION_REQUIRED;
        }

        @Override
        public int getIsolationLevel() {
            return ISOLATION_READ_UNCOMMITTED;
        }

        @Override
        public int getTimeout() {
            return 30;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public String getName() {
            return TRANS_ATTRIBUTE_NAME;
        }

    }
}
