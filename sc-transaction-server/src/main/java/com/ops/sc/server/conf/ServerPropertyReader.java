package com.ops.sc.server.conf;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ServerPropertyReader {

    @Getter
    private static final ServerPropertyReader INSTANCE = new ServerPropertyReader();

    private static final String PROPERTIES_PATH = "config/application.properties";

    private final Properties properties;

    private ServerPropertyReader() {
        properties = getProperties();
    }

    private Properties getProperties() {
        Properties result = new Properties();
        try (InputStream fileInputStream = ServerPropertyReader.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH)) {
            result.load(fileInputStream);
        } catch (final IOException ex) {
            log.warn("can not load properties from file: '{}'.", PROPERTIES_PATH);
        }
        return result;
    }


    public String getValue(final String key, String defaultValue) {
        String result = properties.getProperty(key, defaultValue);
        return result;
    }


    public String getValue(final String key) {
        String result = properties.getProperty(key);
        return result;
    }


    public int getIntValue(final String key, int defaultValue) {
        String result = properties.getProperty(key);
        if(StringUtils.isEmpty(result)){
            return defaultValue;
        }
        else{
            return Integer.parseInt(result);
        }
    }

}
