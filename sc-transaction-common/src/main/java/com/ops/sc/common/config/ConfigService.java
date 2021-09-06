package com.ops.sc.common.config;

import com.ops.sc.common.utils.DateUtil;

import java.time.Duration;
import java.util.Set;

public abstract class ConfigService {

    /**
     * The constant DEFAULT_CONFIG_TIMEOUT.
     */
    protected static final long DEFAULT_CONFIG_TIMEOUT = 5 * 1000;


    public abstract void register(ConfigType configType, ConfigService configService);


    public short getShort(String dataId, int defaultValue, long timeoutMills) {
        String result = getConfig(dataId, String.valueOf(defaultValue), timeoutMills);
        return Short.parseShort(result);
    }


    public short getShort(String dataId, short defaultValue) {
        return getShort(dataId, defaultValue, DEFAULT_CONFIG_TIMEOUT);
    }


    public short getShort(String dataId) {
        return getShort(dataId, (short) 0);
    }


    public int getInt(String dataId, int defaultValue, long timeoutMills) {
        String result = getConfig(dataId, String.valueOf(defaultValue), timeoutMills);
        return Integer.parseInt(result);
    }


    public int getInt(String dataId, int defaultValue) {
        return getInt(dataId, defaultValue, DEFAULT_CONFIG_TIMEOUT);
    }


    public int getInt(String dataId) {
        return getInt(dataId, 0);
    }


    public long getLong(String dataId, long defaultValue, long timeoutMills) {
        String result = getConfig(dataId, String.valueOf(defaultValue), timeoutMills);
        return Long.parseLong(result);
    }


    public long getLong(String dataId, long defaultValue) {
        return getLong(dataId, defaultValue, DEFAULT_CONFIG_TIMEOUT);
    }


    public long getLong(String dataId) {
        return getLong(dataId, 0L);
    }


    public Duration getDuration(String dataId) {
        return getDuration(dataId, Duration.ZERO);
    }


    public Duration getDuration(String dataId, Duration defaultValue) {
        return getDuration(dataId, defaultValue, DEFAULT_CONFIG_TIMEOUT);
    }

    public Duration getDuration(String dataId, Duration defaultValue, long timeoutMills) {
        String result = getConfig(dataId, defaultValue.toMillis() + "ms", timeoutMills);
        return DateUtil.parse(result);
    }


    public boolean getBoolean(String dataId, boolean defaultValue, long timeoutMills) {
        String result = getConfig(dataId, String.valueOf(defaultValue), timeoutMills);
        return Boolean.parseBoolean(result);
    }


    public boolean getBoolean(String dataId, boolean defaultValue) {
        return getBoolean(dataId, defaultValue, DEFAULT_CONFIG_TIMEOUT);
    }


    public boolean getBoolean(String dataId) {
        return getBoolean(dataId, false);
    }


    public String getConfig(String dataId, String defaultValue) {
        return getConfig(dataId, defaultValue, DEFAULT_CONFIG_TIMEOUT);
    }

    public String getConfig(String dataId, long timeoutMills) {
        return getConfig(dataId, null, timeoutMills);
    }


    public String getConfig(String dataId, String content, long timeoutMills) {
        return getLatestConfig(dataId, content, timeoutMills);
    }


    public String getConfig(String dataId) {
        return getConfig(dataId, DEFAULT_CONFIG_TIMEOUT);
    }


    public boolean putConfig(String dataId, String content) {
        return putConfig(dataId, content, DEFAULT_CONFIG_TIMEOUT);
    }


    public boolean putConfigIfAbsent(String dataId, String content) {
        return putConfigIfAbsent(dataId, content, DEFAULT_CONFIG_TIMEOUT);
    }


    public boolean removeConfig(String dataId) {
        return removeConfig(dataId, DEFAULT_CONFIG_TIMEOUT);
    }

    /**
     * Gets type name.
     *
     * @return the type name
     */
    public abstract String getTypeName();

    public abstract String getLatestConfig(String dataId, String defaultValue, long timeoutMills);

    public abstract boolean putConfig(String dataId, String content, long timeoutMills);

    public abstract boolean putConfigIfAbsent(String dataId, String content, long timeoutMills);

    public abstract boolean removeConfig(String dataId, long timeoutMills);

    public abstract Set<ConfigurationChangeListener> getConfigListeners(String dataId);

    public abstract void addConfigListener(String dataId, ConfigurationChangeListener listener);


    public abstract void removeConfigListener(String dataId, ConfigurationChangeListener listener);
}
