package com.ops.sc.core.spi;

import com.ops.sc.common.spi.ServiceLoaderException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerServiceLoader {

    private static final ConcurrentMap<String, TransProcessorSPI> TRANS_PROCESSOR_SERVICES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<? extends TransProcessorSPI>, ConcurrentMap<String, Class<? extends TransProcessorSPI>>> TRANS_PROCESSOR_SERVICE_CLASSES = new ConcurrentHashMap<>();


    public static <T extends TransProcessorSPI> void registerProcessorService(final Class<? extends TransProcessorSPI> transService) {
        TransProcessorSPI transProcessorSPI=(TransProcessorSPI)newServiceInstance(transService);
        if (TRANS_PROCESSOR_SERVICES.containsKey(transProcessorSPI.getProcessorName())) {
            return;
        }
        TRANS_PROCESSOR_SERVICES.putIfAbsent(transProcessorSPI.getProcessorName(),transProcessorSPI);
        //ServiceLoader.load(transService).forEach(each -> registerProcessorServiceClass(transService, each));
    }


    /*private static <T extends TransProcessorSPI> void registerProcessorServiceClass(final Class<T> typedService, final TransProcessorSPI instance) {
        TRANS_PROCESSOR_SERVICES.putIfAbsent(instance.getProcessorName(), instance);
        TRANS_PROCESSOR_SERVICE_CLASSES.computeIfAbsent(typedService, unused -> new ConcurrentHashMap<>()).putIfAbsent(instance.getProcessorName(), instance.getClass());
    }*/


    public static TransProcessorSPI getCachedProcessorServiceInstance(final String mode) {
        return TRANS_PROCESSOR_SERVICES.get(mode);
    }




    public static <T extends TransProcessorSPI> Optional<T> newTypedServiceInstance(final Class<T> typedServiceInterface, final String mode, final Map<String,String> props) {
        Optional<T> result = Optional.ofNullable(TRANS_PROCESSOR_SERVICE_CLASSES.get(typedServiceInterface)).map(serviceClasses -> serviceClasses.get(mode)).map(clazz -> (T) newServiceInstance(clazz));
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
}
