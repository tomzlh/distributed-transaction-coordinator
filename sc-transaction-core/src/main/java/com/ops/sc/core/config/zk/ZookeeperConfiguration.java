
package com.ops.sc.core.config.zk;


import com.ops.sc.common.config.*;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.ConfigurationChangeType;
import com.ops.sc.common.thread.NamedThreadFactory;
import com.ops.sc.common.utils.StringTools;
import com.ops.sc.common.reg.RegConfFactory;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;


public class ZookeeperConfiguration extends ConfigService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfiguration.class);

    private static final String CONFIG_TYPE = "zk";
    private static final String ZK_PATH_SPLIT_CHAR = "/";
    private static final String ROOT_PATH = ZK_PATH_SPLIT_CHAR;
    private static final String SERVER_ADDR_KEY = "serverAddr";
    private static final String SESSION_TIMEOUT_KEY = "sessionTimeout";
    private static final String CONNECT_TIMEOUT_KEY = "connectTimeout";
    private static final String AUTH_USERNAME = "username";
    private static final String AUTH_PASSWORD = "password";
    private static final String SERIALIZER_KEY = "serializer";
    private static final String CONFIG_PATH_KEY = "nodePath";
    private static final int THREAD_POOL_NUM = 1;
    private static final int DEFAULT_SESSION_TIMEOUT = 6000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 2000;
    private static final String DEFAULT_CONFIG_PATH = ROOT_PATH + "/config/registry.properties";
    private static final String FILE_CONFIG_KEY_PREFIX = Constants.FILE_ROOT_CONFIG + Constants.FILE_CONFIG_SPLIT_CHAR + CONFIG_TYPE
            + Constants.FILE_CONFIG_SPLIT_CHAR;
    private static final ExecutorService CONFIG_EXECUTOR = new ThreadPoolExecutor(THREAD_POOL_NUM, THREAD_POOL_NUM,
            Integer.MAX_VALUE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new NamedThreadFactory("ZKConfigThread", THREAD_POOL_NUM));
    private static volatile ZkClient zkClient;
    private static final int MAP_INITIAL_CAPACITY = 8;
    private static final ConcurrentMap<String, ConcurrentMap<ConfigurationChangeListener, ZKListener>> CONFIG_LISTENERS_MAP
            = new ConcurrentHashMap<>(MAP_INITIAL_CAPACITY);
    private static volatile Properties scConfig = new Properties();

    /**
     * Instantiates a new Zookeeper configuration.
     */
    public ZookeeperConfiguration() {
        if (zkClient == null) {
            synchronized (ZookeeperConfiguration.class) {
                if (zkClient == null) {
                    ZkSerializer zkSerializer = getZkSerializer();
                    String serverAddr = PropertyResolver.getINSTANCE().getValue(FILE_CONFIG_KEY_PREFIX + SERVER_ADDR_KEY);
                    int sessionTimeout = PropertyResolver.getINSTANCE().getIntValue(FILE_CONFIG_KEY_PREFIX + SESSION_TIMEOUT_KEY, DEFAULT_SESSION_TIMEOUT);
                    int connectTimeout = PropertyResolver.getINSTANCE().getIntValue(FILE_CONFIG_KEY_PREFIX + CONNECT_TIMEOUT_KEY, DEFAULT_CONNECT_TIMEOUT);
                    zkClient = new ZkClient(serverAddr, sessionTimeout, connectTimeout, zkSerializer);
                    String username = PropertyResolver.getINSTANCE().getValue(FILE_CONFIG_KEY_PREFIX + AUTH_USERNAME);
                    String password = PropertyResolver.getINSTANCE().getValue(FILE_CONFIG_KEY_PREFIX + AUTH_PASSWORD);
                    if (!StringTools.isBlank(username) && !StringTools.isBlank(password)) {
                        StringBuilder auth = new StringBuilder(username).append(":").append(password);
                        zkClient.addAuthInfo("digest", auth.toString().getBytes());
                    }
                }
            }
            if (!zkClient.exists(ROOT_PATH)) {
                zkClient.createPersistent(ROOT_PATH, true);
            }
            initConfig();
            register(ConfigType.ZK,this);
        }
    }

    @Override
    public void register(ConfigType configType, ConfigService configService) {
        RegConfFactory.getInstance().registerConfiguration(configType,configService);
    }

    @Override
    public String getTypeName() {
        return CONFIG_TYPE;
    }

    @Override
    public String getLatestConfig(String dataId, String defaultValue, long timeoutMills) {
        String value = getConfigFromSysPro(dataId);
        if (value != null) {
            return value;
        }

        value = scConfig.getProperty(dataId);
        if (value != null) {
            return value;
        }

        FutureTask<String> future = new FutureTask<>(() -> {
            String path = ROOT_PATH + ZK_PATH_SPLIT_CHAR + dataId;
            if (!zkClient.exists(path)) {
                LOGGER.warn("config {} is not existed, return defaultValue {} ",
                        dataId, defaultValue);
                return defaultValue;
            }
            String value1 = zkClient.readData(path);
            return StringTools.isEmpty(value1) ? defaultValue : value1;
        });
        CONFIG_EXECUTOR.execute(future);
        try {
            return future.get(timeoutMills, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("getConfig {} error or timeout, return defaultValue {}, exception:{} ",
                    dataId, defaultValue, e.getMessage());
            return defaultValue;
        }
    }

    @Override
    public boolean putConfig(String dataId, String content, long timeoutMills) {
        if (!scConfig.isEmpty()) {
            scConfig.setProperty(dataId, content);
            zkClient.writeData(getConfigPath(), getConfigStr());
            return true;
        }

        FutureTask<Boolean> future = new FutureTask<>(() -> {
            String path = ROOT_PATH + ZK_PATH_SPLIT_CHAR + dataId;
            if (!zkClient.exists(path)) {
                zkClient.create(path, content, CreateMode.PERSISTENT);
            } else {
                zkClient.writeData(path, content);
            }
            return true;
        });
        CONFIG_EXECUTOR.execute(future);
        try {
            return future.get(timeoutMills, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("putConfig {}, value: {} is error or timeout, exception: {}",
                    dataId, content, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean putConfigIfAbsent(String dataId, String content, long timeoutMills) {
        throw new RuntimeException("not support atomic operation putConfigIfAbsent");
    }

    @Override
    public boolean removeConfig(String dataId, long timeoutMills) {
        if (!scConfig.isEmpty()) {
            scConfig.remove(dataId);
            zkClient.writeData(getConfigPath(), getConfigStr());
            return true;
        }

        FutureTask<Boolean> future = new FutureTask<>(() -> {
            String path = ROOT_PATH + ZK_PATH_SPLIT_CHAR + dataId;
            return zkClient.delete(path);
        });
        CONFIG_EXECUTOR.execute(future);
        try {
            return future.get(timeoutMills, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("removeConfig {} is error or timeout, exception:{}", dataId, e.getMessage());
            return false;
        }

    }

    @Override
    public void addConfigListener(String dataId, ConfigurationChangeListener listener) {
        if (StringTools.isBlank(dataId) || listener == null) {
            return;
        }

        if (!scConfig.isEmpty()) {
            ZKListener zkListener = new ZKListener(dataId, listener);
            CONFIG_LISTENERS_MAP.computeIfAbsent(dataId, key -> new ConcurrentHashMap<>())
                    .put(listener, zkListener);
            return;
        }

        String path = ROOT_PATH + ZK_PATH_SPLIT_CHAR + dataId;
        if (zkClient.exists(path)) {
            ZKListener zkListener = new ZKListener(path, listener);
            CONFIG_LISTENERS_MAP.computeIfAbsent(dataId, key -> new ConcurrentHashMap<>())
                    .put(listener, zkListener);
            zkClient.subscribeDataChanges(path, zkListener);
        }
    }

    @Override
    public void removeConfigListener(String dataId, ConfigurationChangeListener listener) {
        if (StringTools.isBlank(dataId) || listener == null) {
            return;
        }
        Set<ConfigurationChangeListener> configChangeListeners = getConfigListeners(dataId);
        if (configChangeListeners!=null) {
            String path = ROOT_PATH + ZK_PATH_SPLIT_CHAR + dataId;
            if (zkClient.exists(path)) {
                for (ConfigurationChangeListener entry : configChangeListeners) {
                    if (listener.equals(entry)) {
                        ZKListener zkListener = null;
                        Map<ConfigurationChangeListener, ZKListener> configListeners = CONFIG_LISTENERS_MAP.get(dataId);
                        if (configListeners != null) {
                            zkListener = configListeners.get(listener);
                            configListeners.remove(entry);
                        }
                        if (zkListener != null) {
                            zkClient.unsubscribeDataChanges(path, zkListener);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Set<ConfigurationChangeListener> getConfigListeners(String dataId) {
        ConcurrentMap<ConfigurationChangeListener, ZKListener> configListeners = CONFIG_LISTENERS_MAP.get(dataId);
        if (configListeners!=null) {
            return configListeners.keySet();
        } else {
            return null;
        }
    }

    private void initConfig() {
        String configPath = getConfigPath();
        String config = zkClient.readData(configPath, true);
        if (!StringTools.isEmpty(config)) {
            try (Reader reader = new InputStreamReader(new ByteArrayInputStream(config.getBytes()), StandardCharsets.UTF_8)) {
                scConfig.load(reader);
            } catch (IOException e) {
                LOGGER.error("init config properties error", e);
            }
            ZKListener zkListener = new ZKListener(configPath, null);
            zkClient.subscribeDataChanges(configPath, zkListener);
        }
    }

    private static String getConfigPath() {
        return PropertyResolver.getINSTANCE().getValue(FILE_CONFIG_KEY_PREFIX + CONFIG_PATH_KEY, DEFAULT_CONFIG_PATH);
    }

    private static String getConfigStr() {
        StringBuilder sb = new StringBuilder();

        Enumeration<?> enumeration = scConfig.propertyNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            String property = scConfig.getProperty(key);
            sb.append(key).append("=").append(property).append("\n");
        }

        return sb.toString();
    }

    /**
     * The type Zk listener.
     */
    public static class ZKListener implements IZkDataListener {

        private String path;
        private ConfigurationChangeListener listener;

        /**
         * Instantiates a new Zk listener.
         *
         * @param path     the path
         * @param listener the listener
         */
        public ZKListener(String path, ConfigurationChangeListener listener) {
            this.path = path;
            this.listener = listener;
        }

        @Override
        public void handleDataChange(String s, Object o) {
            if (s.equals(getConfigPath())) {
                Properties newProperties = new Properties();
                if (!StringTools.isBlank(o.toString())) {
                    try (Reader reader = new InputStreamReader(new ByteArrayInputStream(o.toString().getBytes()), StandardCharsets.UTF_8)) {
                        newProperties.load(reader);
                    } catch (IOException e) {
                        LOGGER.error("load config properties error", e);
                        return;
                    }
                }

                for (Map.Entry<String, ConcurrentMap<ConfigurationChangeListener, ZKListener>> entry : CONFIG_LISTENERS_MAP.entrySet()) {
                    String listenedDataId = entry.getKey();
                    String propertyOld = scConfig.getProperty(listenedDataId, "");
                    String propertyNew = newProperties.getProperty(listenedDataId, "");
                    if (!propertyOld.equals(propertyNew)) {
                        ConfigurationChangeEvent event = new ConfigurationChangeEvent()
                                .setDataId(listenedDataId)
                                .setNewValue(propertyNew)
                                .setChangeType(ConfigurationChangeType.MODIFY);

                        ConcurrentMap<ConfigurationChangeListener, ZKListener> configListeners = entry.getValue();
                        for (ConfigurationChangeListener configListener : configListeners.keySet()) {
                            configListener.onProcessEvent(event);
                        }
                    }
                }
                scConfig = newProperties;

                return;
            }
            String dataId = s.replaceFirst(ROOT_PATH + ZK_PATH_SPLIT_CHAR, "");
            ConfigurationChangeEvent event = new ConfigurationChangeEvent().setDataId(dataId).setNewValue(o.toString())
                .setChangeType(ConfigurationChangeType.MODIFY);
            listener.onProcessEvent(event);
        }

        @Override
        public void handleDataDeleted(String s) {
            String dataId = s.replaceFirst(ROOT_PATH + ZK_PATH_SPLIT_CHAR, "");
            ConfigurationChangeEvent event = new ConfigurationChangeEvent().setDataId(dataId).setChangeType(
                    ConfigurationChangeType.DELETE);
            listener.onProcessEvent(event);
        }
    }

    private ZkSerializer getZkSerializer() {
        ZkSerializer zkSerializer = null;
        String serializer = PropertyResolver.getINSTANCE().getValue(FILE_CONFIG_KEY_PREFIX + SERIALIZER_KEY);
        if (!StringTools.isBlank(serializer)) {
            try {
                Class<?> clazz = Class.forName(serializer);
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                zkSerializer = (ZkSerializer) constructor.newInstance();
            } catch (ClassNotFoundException cfe) {
                LOGGER.warn("No zk serializer class found, serializer:{}", serializer, cfe);
            } catch (Throwable cause) {
                LOGGER.warn("found zk serializer encountered an unknown exception", cause);
            }
        }
        if (zkSerializer == null) {
            zkSerializer = new DefaultZkSerializer();
            LOGGER.info("Use default zk serializer: io.config.zk.DefaultZkSerializer.");
        }
        return zkSerializer;
    }

    String getConfigFromSysPro(String dataId) {
        return System.getProperty(dataId);
    }
}
