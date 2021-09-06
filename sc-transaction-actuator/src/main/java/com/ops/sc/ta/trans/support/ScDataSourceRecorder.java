package com.ops.sc.ta.trans.support;


import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.ta.trans.datasource.ScDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ScDataSourceRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScDataSourceRecorder.class);

    private static final Map<String, ScDataSource> DATASOURCE_MAP = new ConcurrentHashMap<>();
    private static ScDataSource defaultDataSource = null;
    private static String defaultDataSourceBeanName = null;

    public static void registerScDataSource(String beanName, ScDataSource scDataSource) {
        LOGGER.debug("Register dataSource for {}", beanName);
        DATASOURCE_MAP.put(beanName, scDataSource);
        if (defaultDataSource == null) {
            defaultDataSourceBeanName = beanName;
            defaultDataSource = scDataSource;
        }
        if (scDataSource.isDefault()) {
            defaultDataSourceBeanName = beanName;
            defaultDataSource = scDataSource;
            LOGGER.info("Default datasource, beanName: {}, bean: {}.", beanName, scDataSource);
        }
    }




    public static ScDataSource getDefaultDataSource() throws ScClientException{
        if (defaultDataSource == null) {
            throw new ScClientException(ClientErrorCode.CLIENT_DATASOURCE_CONFIG_ERROR,
                    "No dataSource found!");
        }
        return defaultDataSource;
    }

    public static String getDefaultDataSourceName() {
        return defaultDataSourceBeanName;
    }

    public static ScDataSource getDataSourceByBeanName(String beanName) {
        return DATASOURCE_MAP.get(beanName);
    }

    public static String getBeanNameByDataSource(ScDataSource scDataSource) {
        for (String bn : DATASOURCE_MAP.keySet()) {
            if (DATASOURCE_MAP.get(bn).equals(scDataSource)) {
                return bn;
            }
        }
        return null;
    }



}
