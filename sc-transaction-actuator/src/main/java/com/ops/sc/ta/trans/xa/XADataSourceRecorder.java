package com.ops.sc.ta.trans.xa;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class XADataSourceRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(XADataSourceRecorder.class);

    private static final Map<String, XADataSource> XA_DATA_SOURCE_MAP = new ConcurrentHashMap<>();
    private static XADataSource defaultXADataSource = null;
    private static String defaultXADataSourceBeanName = null;



    public static void registerXADataSource(String beanName, XADataSource scXADataSource) {
        LOGGER.debug("Register xaDataSource for {}", beanName);
        XA_DATA_SOURCE_MAP.put(beanName, scXADataSource);
        if (defaultXADataSource == null) {
            defaultXADataSource = scXADataSource;
            defaultXADataSourceBeanName = beanName;
        }

        if (scXADataSource.isDefault()) {
            defaultXADataSource = scXADataSource;
            defaultXADataSourceBeanName = beanName;
            LOGGER.info("Default xaDatasource, beanName: {}, bean:{}", beanName, scXADataSource);
        }
    }



    public static String getDefaultXADataSourceBeanName() {
        return defaultXADataSourceBeanName;
    }

    public static XADataSource getDefaultXADataSource() {
        return defaultXADataSource;
    }

    public static String getBeanNameByXADataSource(XADataSource xaDataSource) {
        for (String beanName : XA_DATA_SOURCE_MAP.keySet()) {
            if (XA_DATA_SOURCE_MAP.get(beanName).equals(xaDataSource)) {
                return beanName;
            }
        }
        return null;
    }

    public static XADataSource getXADataSourceByBeanName(String beanName) {
        return XA_DATA_SOURCE_MAP.get(beanName);
    }
}
