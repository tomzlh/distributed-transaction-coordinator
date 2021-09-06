package com.ops.sc.mybatis.datasource;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import com.ops.sc.common.enums.DataSourceInstance;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@AutoConfigureAfter(DatabaseInfo.class)
@Configuration
@Component
@EnableTransactionManagement
public class DataSourceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFacade.class);

    @Resource(name = "metaDB")
    private MetaDB metaDB;

    @Resource(name = "lockReadDB")
    private LockReadDB lockReadDB;

    @Resource(name = "lockWriteDB")
    private LockWriteDB lockWriteDB;

    @Value("${sc.server.deploy.idc.role.slave:false}")
    private boolean idcRoleSlave;

    @Value("${sc.server.db.meta.lock.split:false}")
    private boolean metaLockSplit;

    @Bean(destroyMethod = "close", name = "metaDataSource")
    @Qualifier("metaDataSource")
    @Primary
    public BasicDataSource metaDataSource() {
        LOGGER.info("Ready to create metaDB dataSource");
        return getDbcpBasicDataSource(metaDB);
    }

    @Bean(destroyMethod = "close", name = "lockReadDataSource")
    @Qualifier("lockReadDataSource")
    @ConditionalOnProperty(name = "sc.server.deploy.idc.role.slave", havingValue = "true")
    public BasicDataSource lockReadDataSource() {
        LOGGER.info("Ready to create lockReadDB dataSource");
        return getDbcpBasicDataSource(lockReadDB);
    }

    @Bean(destroyMethod = "close", name = "lockWriteDataSource")
    @Qualifier("lockWriteDataSource")
    @ConditionalOnProperty(name = "sc.server.db.meta.lock.split", havingValue = "true")
    public BasicDataSource lockWriteDataSource() {
        LOGGER.info("Ready to create lockWriteDB dataSource");
        return getDbcpBasicDataSource(lockWriteDB);
    }

    @Bean(name = "multipleDataSource")
    @Qualifier("multipleDataSource")
    public MultipleDataSource multipleDataSource() {
        MultipleDataSource multipleDataSource = new MultipleDataSource();
        multipleDataSource.setDefaultTargetDataSource(metaDataSource());
        Map<Object, Object> map = new HashMap<>();
        // 多机房部署，meta和lock分离
        if (metaLockSplit) {
            map.put(DataSourceInstance.LockWrite, lockWriteDataSource());
        }
        // 多机房部署，slave的LockStore读写分离
        if (idcRoleSlave) {
            map.put(DataSourceInstance.LockRead, lockReadDataSource());
        }
        multipleDataSource.setTargetDataSources(map);
        return multipleDataSource;
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean() {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(multipleDataSource());
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            bean.setMapperLocations(resolver.getResources("classpath:mapper/*Mapper.xml"));
            bean.setConfigLocation(resolver.getResource("classpath:mybatis-config.xml"));
            return bean.getObject();
        } catch (Exception ex) {
            LOGGER.error("Error when create sqlSessionFactoryBean", ex);
            throw new RuntimeException(ex);
        }
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(multipleDataSource());
    }

    private BasicDataSource getDbcpBasicDataSource(DatabaseInfo db) {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(db.getDriverClassName());
        basicDataSource.setUrl(db.getUrl());
        basicDataSource.setPassword(db.getPassword());
        basicDataSource.setUsername(db.getUsername());
        basicDataSource.setMaxIdle(db.getMaxIdle());
        basicDataSource.setMinIdle(db.getMinIdle());
        basicDataSource.setInitialSize(db.getInitialSize());
        basicDataSource.setMaxTotal(db.getMaxTotal());
        basicDataSource.setMinEvictableIdleTimeMillis(db.getMinEvictIdleTimeMillis());
        basicDataSource.setTimeBetweenEvictionRunsMillis(db.getTimeEvictRunMillis());
        basicDataSource.setTestOnBorrow(db.isTestOnBorrow());
        basicDataSource.setTestWhileIdle(db.isTestWhileIdle());
        basicDataSource.setValidationQuery(db.getValidationQuery());
        basicDataSource.setNumTestsPerEvictionRun(db.getNumTestsPerEvictRun());
        return basicDataSource;
    }

}
