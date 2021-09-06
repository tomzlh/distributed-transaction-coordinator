package com.ops.sc.mybatis.datasource;

import javax.sql.DataSource;

import com.ops.sc.common.enums.DataSourceInstance;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


public class MultipleDataSource extends AbstractRoutingDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleDataSource.class);

    private static final ThreadLocal<DataSourceInstance> dataSourceKey = new InheritableThreadLocal<DataSourceInstance>() {
        @Override
        protected DataSourceInstance initialValue() {
            return DataSourceInstance.MetaData;
        }
    };

    static void setDataSourceKey(DataSourceInstance dataSource) {
        dataSourceKey.set(dataSource);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return dataSourceKey.get();
    }

    @Override
    protected DataSource determineTargetDataSource() {
        DataSource dataSource = super.determineTargetDataSource();
        LOGGER.debug("DetermineCurrentLookupKey: {}, dbUrl: {}", determineCurrentLookupKey(),
                ((BasicDataSource) dataSource).getUrl());
        return dataSource;
    }

}
