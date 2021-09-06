package com.ops.sc.common.classloader;

import com.ops.sc.common.anno.LoadLevel;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.Scope;
import com.ops.sc.common.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class CommonServiceLoader {

    public static <S> S load(Class<S> service, ClassLoader loader) throws ServiceClassNotFoundException {
        return InnerEnhancedServiceLoader.getServiceLoader(service).load(loader);
    }


    public static <S> S load(Class<S> service) throws ServiceClassNotFoundException {
        return InnerEnhancedServiceLoader.getServiceLoader(service).load(findClassLoader());
    }


    public static <S> S load(Class<S> service, String activateName) throws ServiceClassNotFoundException {
        return InnerEnhancedServiceLoader.getServiceLoader(service).load(activateName, findClassLoader());
    }


    public static <S> S load(Class<S> service, String activateName, ClassLoader loader)
            throws ServiceClassNotFoundException {
        return InnerEnhancedServiceLoader.getServiceLoader(service).load(activateName, loader);
    }


    public static <S> S load(Class<S> service, String activateName, Object[] args)
            throws ServiceClassNotFoundException {
        return InnerEnhancedServiceLoader.getServiceLoader(service).load(activateName, args, findClassLoader());
    }


    public static <S> S load(Class<S> service, String activateName, Class[] argsType, Object[] args)
            throws ServiceClassNotFoundException {
        return InnerEnhancedServiceLoader.getServiceLoader(service).load(activateName, argsType, args, findClassLoader());
    }

    /**
     * get all implements
     *
     * @param <S>     the type parameter
     * @param service the service
     * @return list list
     */
    public static <S> List<S> loadAll(Class<S> service) {
        return InnerEnhancedServiceLoader.getServiceLoader(service).loadAll(findClassLoader());
    }

    /**
     * get all implements
     *
     * @param <S>      the type parameter
     * @param service  the service
     * @param argsType the args type
     * @param args     the args
     * @return list list
     */
    public static <S> List<S> loadAll(Class<S> service, Class[] argsType, Object[] args) {
        return InnerEnhancedServiceLoader.getServiceLoader(service).loadAll(argsType, args, findClassLoader());
    }


    @SuppressWarnings("rawtypes")
    static <S> List<Class> getAllExtensionClass(Class<S> service) {
        return InnerEnhancedServiceLoader.getServiceLoader(service).getAllExtensionClass(findClassLoader());
    }


    @SuppressWarnings("rawtypes")
    static <S> List<Class> getAllExtensionClass(Class<S> service, ClassLoader loader) {
        return InnerEnhancedServiceLoader.getServiceLoader(service).getAllExtensionClass(loader);
    }


    private static ClassLoader findClassLoader() {
        return CommonServiceLoader.class.getClassLoader();
    }


    private static class InnerEnhancedServiceLoader<S> {
        private static final Logger LOGGER = LoggerFactory.getLogger(InnerEnhancedServiceLoader.class);
        private static final String SERVICES_DIRECTORY = "META-INF/services/";
        private static final String EXTENSION_DIRECTORY = "META-INF/extension/";

        private static final ConcurrentMap<Class<?>, InnerEnhancedServiceLoader<?>> SERVICE_LOADERS =
                new ConcurrentHashMap<>();

        private final Class<S> type;
        private final Holder<List<ExtensionInfo>> definitionsHolder = new Holder<>();
        private final ConcurrentMap<ExtensionInfo, Holder<Object>> definitionToInstanceMap =
                new ConcurrentHashMap<>();
        private final ConcurrentMap<String, List<ExtensionInfo>> nameToDefinitionsMap = new ConcurrentHashMap<>();
        private final ConcurrentMap<Class<?>, ExtensionInfo> classToDefinitionMap = new ConcurrentHashMap<>();

        private InnerEnhancedServiceLoader(Class<S> type) {
            this.type = type;
        }

        /**
         * Get the ServiceLoader for the specified Class
         *
         * @param type the type of the extension point
         * @param <S>  the type
         * @return the service loader
         */
        private static <S> InnerEnhancedServiceLoader<S> getServiceLoader(Class<S> type) {
            if (type == null) {
                throw new IllegalArgumentException("Enhanced Service type == null");
            }
            return (InnerEnhancedServiceLoader<S>)SERVICE_LOADERS.computeIfAbsent(type,
                    key -> new InnerEnhancedServiceLoader<>(type));
        }


        private S load(ClassLoader loader) throws ServiceClassNotFoundException {
            return loadExtension(loader, null, null);
        }


        private S load(String activateName, ClassLoader loader)
                throws ServiceClassNotFoundException {
            return loadExtension(activateName, loader, null, null);
        }


        private S load(String activateName, Object[] args, ClassLoader loader)
                throws ServiceClassNotFoundException {
            Class[] argsType = null;
            if (args != null && args.length > 0) {
                argsType = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    argsType[i] = args[i].getClass();
                }
            }
            return loadExtension(activateName, loader, argsType, args);
        }


        private S load(String activateName, Class[] argsType, Object[] args, ClassLoader loader)
                throws ServiceClassNotFoundException {
            return loadExtension(activateName, loader, argsType, args);
        }

        /**
         * get all implements
         * @param loader  the class loader
         *
         * @return list list
         */
        private List<S> loadAll(ClassLoader loader) {
            return loadAll(null, null, loader);
        }

        /**
         * get all implements
         *
         * @param argsType the args type
         * @param args     the args
         * @return list list
         */
        private List<S> loadAll(Class[] argsType, Object[] args, ClassLoader loader) {
            List<S> allInstances = new ArrayList<>();
            List<Class> allClazz = getAllExtensionClass(loader);
            if (allClazz==null||allClazz.isEmpty()) {
                return allInstances;
            }
            try {
                for (Class clazz : allClazz) {
                    ExtensionInfo definition = classToDefinitionMap.get(clazz);
                    allInstances.add(getExtensionInstance(definition, loader, argsType, args));
                }
            } catch (Throwable t) {
                throw new ServiceClassNotFoundException(t);
            }
            return allInstances;
        }

        /**
         * Get all the extension classes, follow {@linkplain LoadLevel} defined and sort order
         *
         * @param loader the loader
         * @return all extension class
         */
        @SuppressWarnings("rawtypes")
        private List<Class> getAllExtensionClass(ClassLoader loader) {
            return loadAllExtensionClass(loader);
        }

        @SuppressWarnings("rawtypes")
        private S loadExtension(ClassLoader loader, Class[] argTypes,
                                Object[] args) {
            try {
                loadAllExtensionClass(loader);
                ExtensionInfo defaultExtensionDefinition = getDefaultExtensionDefinition();
                return getExtensionInstance(defaultExtensionDefinition, loader, argTypes, args);
            } catch (Throwable e) {
                if (e instanceof ServiceClassNotFoundException) {
                    throw (ServiceClassNotFoundException)e;
                } else {
                    throw new ServiceClassNotFoundException(
                            "not found service provider for : " + type.getName() + " caused by " + e.getMessage());
                }
            }
        }

        @SuppressWarnings("rawtypes")
        private S loadExtension(String activateName, ClassLoader loader, Class[] argTypes,
                                Object[] args) {
            if (StringTools.isEmpty(activateName)) {
                throw new IllegalArgumentException("the name of service provider for [" + type.getName() + "] name is null");
            }
            try {
                loadAllExtensionClass(loader);
                ExtensionInfo cachedExtensionDefinition = getCachedExtensionDefinition(activateName);
                return getExtensionInstance(cachedExtensionDefinition, loader, argTypes, args);
            } catch (Throwable e) {
                if (e instanceof ServiceClassNotFoundException) {
                    throw (ServiceClassNotFoundException)e;
                } else {
                    throw new ServiceClassNotFoundException(
                            "not found service provider for : " + type.getName() + " caused by " + e.getMessage());
                }
            }
        }

        private S getExtensionInstance(ExtensionInfo definition, ClassLoader loader, Class[] argTypes,
                                       Object[] args) {
            if (definition == null) {
                throw new ServiceClassNotFoundException("not found service provider for : " + type.getName());
            }
            if (Scope.SINGLETON.equals(definition.getScope())) {
                Holder<Object> holder = definitionToInstanceMap.computeIfAbsent(definition,
                        key -> new Holder<>());
                Object instance = holder.get();
                if (instance == null) {
                    synchronized (holder) {
                        instance = holder.get();
                        if (instance == null) {
                            instance = createNewExtension(definition, loader, argTypes, args);
                            holder.set(instance);
                        }
                    }
                }
                return (S)instance;
            } else {
                return createNewExtension(definition, loader, argTypes, args);
            }
        }

        private S createNewExtension(ExtensionInfo definition, ClassLoader loader, Class[] argTypes, Object[] args) {
            Class<?> clazz = definition.getServiceClass();
            try {
                S newInstance = initInstance(clazz, argTypes, args);
                return newInstance;
            } catch (Throwable t) {
                throw new IllegalStateException("Extension instance(definition: " + definition + ", class: " +
                        type + ")  could not be instantiated: " + t.getMessage(), t);
            }
        }

        private List<Class> loadAllExtensionClass(ClassLoader loader) {
            List<ExtensionInfo> definitions = definitionsHolder.get();
            if (definitions == null) {
                synchronized (definitionsHolder) {
                    definitions = definitionsHolder.get();
                    if (definitions == null) {
                        definitions = findAllExtensionDefinition(loader);
                        definitionsHolder.set(definitions);
                    }
                }
            }
            return definitions.stream().map(def -> def.getServiceClass()).collect(Collectors.toList());
        }

        @SuppressWarnings("rawtypes")
        private List<ExtensionInfo> findAllExtensionDefinition(ClassLoader loader) {
            List<ExtensionInfo> extensionDefinitions = new ArrayList<>();
            try {
                loadFile(SERVICES_DIRECTORY, loader, extensionDefinitions);
                loadFile(EXTENSION_DIRECTORY, loader, extensionDefinitions);
            } catch (IOException e) {
                throw new ServiceClassNotFoundException(e);
            }

            //After loaded all the extensions,sort the caches by order
            if (!nameToDefinitionsMap.isEmpty()) {
                for (List<ExtensionInfo> definitions : nameToDefinitionsMap.values()) {
                    Collections.sort(definitions, (def1, def2) -> {
                        int o1 = def1.getOrder();
                        int o2 = def2.getOrder();
                        return Integer.compare(o1, o2);
                    });
                }
            }

            if (!extensionDefinitions.isEmpty()) {
                Collections.sort(extensionDefinitions, (definition1, definition2) -> {
                    int o1 = definition1.getOrder();
                    int o2 = definition2.getOrder();
                    return Integer.compare(o1, o2);
                });
            }

            return extensionDefinitions;
        }


        @SuppressWarnings("rawtypes")
        private void loadFile(String dir, ClassLoader loader, List<ExtensionInfo> extensions)
                throws IOException {
            String fileName = dir + type.getName();
            Enumeration<URL> urls;
            if (loader != null) {
                urls = loader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), Constants.DEFAULT_CHARSET))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            final int ci = line.indexOf('#');
                            if (ci >= 0) {
                                line = line.substring(0, ci);
                            }
                            line = line.trim();
                            if (line.length() > 0) {
                                try {
                                    ExtensionInfo extensionDefinition = getUnloadedExtensionDefinition(line, loader);
                                    if (extensionDefinition == null) {
                                        if (LOGGER.isDebugEnabled()) {
                                            LOGGER.debug("The same extension {} has already been loaded, skipped", line);
                                        }
                                        continue;
                                    }
                                    extensions.add(extensionDefinition);
                                } catch (LinkageError | ClassNotFoundException e) {
                                    LOGGER.warn("Load [{}] class fail. {}", line, e.getMessage());
                                }
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.warn("load clazz instance error: {}", e.getMessage());
                    }
                }
            }
        }

        private ExtensionInfo getUnloadedExtensionDefinition(String className, ClassLoader loader)
                throws ClassNotFoundException {
            //Check whether the definition has been loaded
            if (!isDefinitionContainsClazz(className, loader)) {
                Class<?> clazz = Class.forName(className, true, loader);
                String serviceName = null;
                Integer priority = 0;
                Scope scope = Scope.SINGLETON;
                LoadLevel loadLevel = clazz.getAnnotation(LoadLevel.class);
                if (loadLevel != null) {
                    serviceName = loadLevel.name();
                    priority = loadLevel.order();
                    scope = loadLevel.scope();
                }
                ExtensionInfo result = new ExtensionInfo(serviceName, priority, scope, clazz);
                classToDefinitionMap.put(clazz, result);
                if (serviceName != null) {
                    nameToDefinitionsMap.computeIfAbsent(serviceName, e -> new ArrayList<>())
                            .add(result);
                }
                return result;
            }
            return null;
        }

        private boolean isDefinitionContainsClazz(String className, ClassLoader loader) {
            for (Map.Entry<Class<?>, ExtensionInfo> entry : classToDefinitionMap.entrySet()) {
                if (!entry.getKey().getName().equals(className)) {
                    continue;
                }
                if (Objects.equals(entry.getValue().getServiceClass().getClassLoader(), loader)) {
                    return true;
                }
            }
            return false;
        }

        private ExtensionInfo getDefaultExtensionDefinition() {
            List<ExtensionInfo> currentDefinitions = definitionsHolder.get();
            return currentDefinitions.get(currentDefinitions.size()-1);
        }

        private ExtensionInfo getCachedExtensionDefinition(String activateName) {
            List<ExtensionInfo> definitions = nameToDefinitionsMap.get(activateName);
            return definitions.get(definitions.size()-1);
        }


        private S initInstance(Class implClazz, Class[] argTypes, Object[] args)
                throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
            S s = null;
            if (argTypes != null && args != null) {
                // Constructor with arguments
                Constructor<S> constructor = implClazz.getDeclaredConstructor(argTypes);
                s = type.cast(constructor.newInstance(args));
            } else {
                // default Constructor
                s = type.cast(implClazz.newInstance());
            }
            return s;
        }

        /**
         * Helper Class for hold a value.
         * @param <T>
         */
        private static class Holder<T> {
            private volatile T value;

            private void set(T value) {
                this.value = value;
            }

            private T get() {
                return value;
            }
        }
    }

}
