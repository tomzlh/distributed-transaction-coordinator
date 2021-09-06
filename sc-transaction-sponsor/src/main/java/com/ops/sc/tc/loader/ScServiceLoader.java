
package com.ops.sc.tc.loader;

import com.ops.sc.common.bean.GlobalTransRequestBean;
import com.ops.sc.common.bean.GlobalTransResponseBean;
import com.ops.sc.common.spi.GlobalTransactionExecutor;
import com.ops.sc.common.spi.SPIPostProcessor;
import com.ops.sc.common.spi.ServiceLoaderException;
import com.ops.sc.tc.service.CommonTransactionProxyService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScServiceLoader {
    
    private static final ConcurrentMap<Class<? extends GlobalTransactionExecutor>, ConcurrentMap<String, GlobalTransactionExecutor>> TRANS_SERVICES = new ConcurrentHashMap<>();
    
    private static final ConcurrentMap<Class<? extends GlobalTransactionExecutor>, ConcurrentMap<String, Class<? extends GlobalTransactionExecutor>>> TRANS_SERVICE_CLASSES = new ConcurrentHashMap<>();
    

    public static <T extends GlobalTransactionExecutor> void registerTransService(final Class<T> service) {
        if (TRANS_SERVICES.containsKey(service)) {
            return;
        }
        ServiceLoader.load(service).forEach(each -> {registerTransServiceClass(service, each);
            proxy(each);
        });

    }
    
    private static <T extends GlobalTransactionExecutor> void registerTransServiceClass(final Class<T> service, final GlobalTransactionExecutor instance) {
        TRANS_SERVICES.computeIfAbsent(service, unused -> new ConcurrentHashMap<>()).putIfAbsent(instance.getTransName(), instance);
        TRANS_SERVICE_CLASSES.computeIfAbsent(service, unused -> new ConcurrentHashMap<>()).putIfAbsent(instance.getTransName(), instance.getClass());
    }
    

    public static <T extends GlobalTransactionExecutor> Optional<T> getCachedServiceInstance(final Class<T> serviceInterface, final String type) {
        return Optional.ofNullable(TRANS_SERVICES.get(serviceInterface)).map(services -> (T) services.get(type));
    }
    

    public static <T extends GlobalTransactionExecutor> Optional<T> newServiceInstance(final Class<T> serviceInterface, final String type, final Properties props) {
        Optional<T> result = Optional.ofNullable(TRANS_SERVICE_CLASSES.get(serviceInterface)).map(serviceClasses -> serviceClasses.get(type)).map(clazz -> (T) newServiceInstance(clazz));
        if (result.isPresent() && result.get() instanceof SPIPostProcessor) {
            ((SPIPostProcessor) result.get()).init(props);
        }
        return result;
    }
    
    private static Object newServiceInstance(final Class<?> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (final InstantiationException | NoSuchMethodException | IllegalAccessException ex) {
            throw new ServiceLoaderException(clazz, ex);
        } catch (final InvocationTargetException ex) {
            throw new ServiceLoaderException(clazz, ex.getCause());
        }
    }

    public static Object proxy(GlobalTransactionExecutor globalTransactionExecutor) {
        return  Enhancer.create(GlobalTransactionExecutor.class,
                (MethodInterceptor)(proxy, method, args, methodProxy) -> {
                    if (method.getName().equals("execute")) {
                        //Object result = methodProxy.invoke(globalTransactionExecutor, args);
                        GlobalTransResponseBean globalTransResponseBean =new GlobalTransResponseBean();
                        if(args!=null&&args.length==1){
                            try {
                                String tid = CommonTransactionProxyService.registerGlobalTrans((GlobalTransRequestBean) args[0]);
                                globalTransResponseBean.setTid(tid);
                                globalTransResponseBean.setStatus("SUCCESS");
                            }catch (final Throwable e){
                                globalTransResponseBean.setTid(null);
                                globalTransResponseBean.setStatus("FAILED");
                            }
                        }
                        return globalTransResponseBean;
                    }
                    return method.invoke(globalTransactionExecutor, args);
        });
    }
}
