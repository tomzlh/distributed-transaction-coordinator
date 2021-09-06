package com.ops.sc.core.trans;

import com.ops.sc.common.utils.ClassLoaderHelper;
import com.ops.sc.core.config.TransMessageConfig;
import com.ops.sc.core.spi.TransMessageSPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;


public class TransMessagePluginLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransMessagePluginLoader.class);

    private static final Map<Integer, Class> PRODUCER_CLASS_MAP = new ConcurrentHashMap<>();

    private static final String PLUGIN_HOME_DIR = System.getProperty("user.dir") + File.separator + "extensions";

    private static Logger logger = LoggerFactory.getLogger(TransMessagePluginLoader.class);

    static {
        logger.info("Start to load trans-message plugin...");
        loadAllProducerClass();
        logger.info("Load all producer class completed.");
    }

    private static void loadAllProducerClass() {
        if (TransMessageConfig.isRunInClientMode()) {
            ServiceLoader<TransMessageSPI> producerProviders = ServiceLoader.load(TransMessageSPI.class);
            for (TransMessageSPI transMessageSpi : producerProviders) {
                registerPlugin(transMessageSpi);
            }
        } else {
            File pluginHome = new File(PLUGIN_HOME_DIR);
            if (pluginHome.exists() && pluginHome.isDirectory()) {
                File[] files = pluginHome.listFiles();
                if (files == null) {
                    return;
                }
                String enablePluginList = TransMessageConfig.getEnablePluginList();
                LOGGER.info("Plugin list: {}", enablePluginList);
                if (StringUtils.isBlank(enablePluginList)) {
                    return;
                }
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (!enablePluginList.contains(file.getName())) {
                            continue;
                        }
                        LOGGER.info("Enable plugin: {}", file.getName());
                        ClassLoaderHelper classLoaderHelper = new ClassLoaderHelper(file.getPath(),
                                TransMessagePluginLoader.class.getClassLoader());
                        ServiceLoader<TransMessageSPI> producerProviders = ServiceLoader
                                .load(TransMessageSPI.class, classLoaderHelper);
                        Iterator<TransMessageSPI> iterator = producerProviders.iterator();
                        // 仅支持一个插件目录完成一个插件注册
                        if (iterator.hasNext()) {
                            TransMessageSPI transMessageSpi = iterator.next();
                            registerPlugin(transMessageSpi);
                        }
                    }
                }
            }
        }
    }

    private static void registerPlugin(TransMessageSPI transMessageSpi) {
        int producerType = transMessageSpi.resolveType();
        if (producerType < 0) {
            throw new IllegalArgumentException("Producer type must not below zero");
        }
        if (PRODUCER_CLASS_MAP.containsKey(producerType)) {
            throw new IllegalStateException("Producer for type:" + producerType + " already register!");
        }
        Class producerClass = transMessageSpi.load();
        PRODUCER_CLASS_MAP.put(producerType, producerClass);
        logger.info("Load provider class:{} for type:{} with classLoader:{} done.", producerClass.getName(),
                producerType, producerClass.getClassLoader());
    }

    public static Class getProducerClass(int producerType) {
        return PRODUCER_CLASS_MAP.get(producerType);
    }

}
