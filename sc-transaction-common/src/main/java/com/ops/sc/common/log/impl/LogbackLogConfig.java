package com.ops.sc.common.log.impl;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import com.ops.sc.common.log.LogConfig;
import com.ops.sc.common.utils.ResourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.impl.StaticLoggerBinder;


public class LogbackLogConfig extends LogConfig {

    private static final String SC_LOGBACK_LOCATION = "classpath:sc-logback-default.xml";


    @Override
    public void loadConfiguration() {

        String location = getConfigPath(SC_LOGBACK_LOCATION);
        if (StringUtils.isBlank(location)) {
            return;
        }
        try {
            LoggerContext loggerContext = (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
            new ContextInitializer(loggerContext).configureByResource(ResourceUtils.getClassUrl(location));
        } catch (Exception e) {
            throw new IllegalStateException("Can not initialize logback logging from " + location, e);
        }

    }

}
