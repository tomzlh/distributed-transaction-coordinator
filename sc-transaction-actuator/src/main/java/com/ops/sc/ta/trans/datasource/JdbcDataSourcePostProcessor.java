package com.ops.sc.ta.trans.datasource;

import com.ops.sc.ta.trans.support.ScDataSourceRecorder;
import com.ops.sc.ta.trans.xa.XADataSource;
import com.ops.sc.ta.trans.xa.XADataSourceRecorder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component("jdbcDataSourcePostProcessor")
public class JdbcDataSourcePostProcessor implements BeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ScDataSource) {
            ScDataSourceRecorder.registerScDataSource(beanName, (ScDataSource) bean);
        } else if (bean instanceof XADataSource) {
            XADataSourceRecorder.registerXADataSource(beanName, (XADataSource) bean);
        }
        return bean;
    }

}
