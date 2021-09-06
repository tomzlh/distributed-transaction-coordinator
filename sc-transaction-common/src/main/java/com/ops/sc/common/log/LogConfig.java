package com.ops.sc.common.log;


import com.ops.sc.common.constant.Constants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class LogConfig {

    protected String getConfigPath(String defaultLocation) {
        String location = System.getProperty(Constants.CONFIG_PROPERTY);
        if (StringUtils.isBlank(location)) {
            if (isDefaultConfigEnabled()) {
                return defaultLocation;
            }
            return null;
        }
        return location;
    }

    /**
     * default value is true
     *
     * @return
     */
    private boolean isDefaultConfigEnabled() {
        String property = System.getProperty(Constants.DEFAULT_CONFIG_ENABLED);
        return property == null || BooleanUtils.toBoolean(property);
    }

    /**
     * load custom config file
     */
    public abstract void loadConfiguration();

}
