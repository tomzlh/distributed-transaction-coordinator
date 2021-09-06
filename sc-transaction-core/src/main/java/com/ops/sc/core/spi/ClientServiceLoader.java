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
public final class ClientServiceLoader {

    private static final ConcurrentMap<Class<? extends TransHandlerSPI>, ConcurrentMap<String, TransHandlerSPI>> TRANS_HANDLER_SERVICES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, TransHandlerSPI> TRANS_HANDLER_INSTANCES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<? extends TransHandlerSPI>, ConcurrentMap<String, Class<? extends TransHandlerSPI>>> TRANS_HANDLER_SERVICE_CLASSES = new ConcurrentHashMap<>();


    public static <T extends TransHandlerSPI> void registerHandlerInstance(final String transactionName, final TransHandlerSPI instance) {
        if (TRANS_HANDLER_INSTANCES.containsKey(transactionName)) {
            return;
        }
        TRANS_HANDLER_INSTANCES.putIfAbsent(transactionName,instance);
    }

     public static <TransHandlerSPI>  Optional<TransHandlerSPI> getCachedHandlerInstance(final String transactionName){
        return Optional.ofNullable(TRANS_HANDLER_INSTANCES.get(transactionName)).map(service -> (TransHandlerSPI) service);
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
