package com.ops.sc.common.utils;


import com.google.protobuf.ByteString;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.enums.ServerMode;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.log.LogConfig;
import com.ops.sc.common.log.impl.Log4J2LogConfig;
import com.ops.sc.common.log.impl.LogbackLogConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import static com.ops.sc.common.constant.Constants.SC_DEFAULT_CHARSET;


public class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);



    public static void initLoggingSystem(Environment environment) {
        setSystemProperty(environment);
        loadConfiguration();
    }


    private static void setSystemProperty(Environment environment) {
        String loggingPath = environment.getProperty(Constants.LOGGING_PATH, Constants.DEFAULT_PATH);
        System.setProperty(Constants.LOGGING_PATH, loggingPath);
        String defaultLoggingConfigEnable = environment.getProperty(Constants.DEFAULT_CONFIG_ENABLED,
                Boolean.TRUE.toString());
        System.setProperty(Constants.DEFAULT_CONFIG_ENABLED, defaultLoggingConfigEnable);
        String loggingConfigLocation = environment.getProperty(Constants.CONFIG_PROPERTY);
        if (StringUtils.isNotBlank(loggingConfigLocation)) {
            System.setProperty(Constants.CONFIG_PROPERTY, loggingConfigLocation);
        }
        String loggingLevel = environment.getProperty(Constants.LOGGING_LEVEL, Constants.DEFAULT_LEVEL);
        System.setProperty(Constants.LOGGING_LEVEL, loggingLevel);
    }


    private static void loadConfiguration() {
        try {
            boolean logbackEnable = false;
            LogConfig logConfig;
            try {
                Class.forName("ch.qos.logback.classic.Logger");
                logConfig = new LogbackLogConfig();
                logbackEnable = true;
            } catch (ClassNotFoundException ex) {
                logConfig = new Log4J2LogConfig();
            }
            try {
                logConfig.loadConfiguration();
            } catch (Throwable t) {
                if (logbackEnable) {
                    LOGGER.warn("Load  logback configuration failed, message: {}", t.getMessage());
                } else {
                    LOGGER.warn("Load  log4j2 configuration failed, message: {}", t.getMessage());
                }
            }
            LOGGER.info("System property logging.default.config.enabled={}",
                    System.getProperty(Constants.DEFAULT_CONFIG_ENABLED));
            LOGGER.info("System property logging.path={}", System.getProperty(Constants.LOGGING_PATH));
            LOGGER.info("System property logging.level={}", System.getProperty(Constants.LOGGING_LEVEL));

            String loggingConfigLocation = System.getProperty(Constants.CONFIG_PROPERTY);
            if (StringUtils.isNotBlank(loggingConfigLocation)) {
                LOGGER.info("Set system property logging.config={}", loggingConfigLocation);
            }

            if (logbackEnable) {
                LOGGER.info("Load logback configuration success");
            } else {
                LOGGER.info("Load log4j2 configuration success");
            }

        } catch (Throwable t1) {
            LOGGER.warn("Init logging configuration failed,message: {}", t1.getMessage());
        }
    }

    private static ServerMode currentFrameMode = ServerMode.REMOTE;

    public static ServerMode getCurrentFrameMode() {
        return currentFrameMode;
    }

    public static void initCurrentFrameMode(Environment environment) {
        boolean isLocal = Boolean.valueOf(environment.getProperty(Constants.LOCAL_MODE_PROPERTY));
        currentFrameMode = isLocal ? ServerMode.LOCAL : ServerMode.REMOTE;
        LOGGER.info("Sc currentFrameMode: {}", currentFrameMode);
    }


    public static String generateLockKey(Object... args) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append(arg);
            sb.append(":");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static ByteString toByteString(String text) {
        return toByteString(text.getBytes(SC_DEFAULT_CHARSET));
    }

    public static ByteString toByteString(byte[] bytes) {
        return ByteString.copyFrom(bytes);
    }

    public static ByteString toByteString(Object[] objectArray) throws ScClientException{
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(objectArray);
            oos.flush();
            byte[] byteArray = bos.toByteArray();
            oos.close();
            bos.close();
            return ByteString.copyFrom(byteArray);
        } catch (IOException e) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED,
                    "get tcc register params failed!", e);
        }
    }

    public static Object[] toObjectArray(byte[] bytes) throws ScClientException{
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object[] objArray = (Object[]) ois.readObject();
            ois.close();
            bis.close();
            return objArray;
        } catch (IOException | ClassNotFoundException e) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, "Parse tcc invoke params failed!",
                    e);
        }
    }

    public static String toString(byte[] bytes) {
        return toStringInternal(toByteString(bytes));
    }

    public static Object[] toObjectArray(ByteString byteString) throws  ScClientException{
        return toObjectArray(toByteArray(byteString));
    }

    public static List<Map<String, Object>> rsToList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> list = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; ++i) {
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }

    public static byte[] toByteArray(ByteString byteString) {
        return byteString.toByteArray();
    }

    public static String toString(ByteString byteString, Charset charset) {
        return byteString.toString(charset);
    }

    public static String toStringInternal(ByteString byteString) {
        return toString(byteString, SC_DEFAULT_CHARSET);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return !isNotEmpty(collection);
    }


    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}
