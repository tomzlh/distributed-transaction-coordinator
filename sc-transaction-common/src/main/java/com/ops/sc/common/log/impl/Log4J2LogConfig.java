package com.ops.sc.common.log.impl;


import com.ops.sc.common.log.LogConfig;
import com.ops.sc.common.utils.ResourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;


public class Log4J2LogConfig extends LogConfig {

    private static final String SC_LOG4J2_LOCATION = "classpath:sc-log4j2-default.xml";

    private static final String FILE_PROTOCOL = "file";

    private static final String SC_LOGGER_PREFIX = "com.op.sc";

    /**
     * load custom config file
     */
    @Override
    public void loadConfiguration() {
        String location = super.getConfigPath(SC_LOG4J2_LOCATION);
        if (StringUtils.isBlank(location)) {
            return;
        }
        final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        final Configuration contextConfiguration = loggerContext.getConfiguration();

        Configuration configuration = loadConfiguration(loggerContext, location);
        configuration.start();

        // append loggers and appender to contextConfiguration
        Map<String, Appender> appenders = configuration.getAppenders();
        for (Appender appender : appenders.values()) {
            contextConfiguration.addAppender(appender);
        }
        Map<String, LoggerConfig> loggers = configuration.getLoggers();
        for (String name : loggers.keySet()) {
            if (name.startsWith(SC_LOGGER_PREFIX)) {
                contextConfiguration.addLogger(name, loggers.get(name));
            }
        }

        loggerContext.updateLoggers();
    }

    private Configuration loadConfiguration(LoggerContext loggerContext, String location) {
        try {
            URL url = ResourceUtils.getClassUrl(location);
            ConfigurationSource source = getConfigurationSource(url);
            return ConfigurationFactory.getInstance().getConfiguration(loggerContext, source);
        } catch (Exception e) {
            throw new IllegalStateException("Can not initialize log4j2 logging from " + location, e);
        }
    }

    private ConfigurationSource getConfigurationSource(URL url) throws IOException {
        InputStream stream = url.openStream();
        if (FILE_PROTOCOL.equals(url.getProtocol())) {
            return new ConfigurationSource(stream, ResourceUtils.getResourceAsFile(url));
        }
        return new ConfigurationSource(stream, url);
    }

}
