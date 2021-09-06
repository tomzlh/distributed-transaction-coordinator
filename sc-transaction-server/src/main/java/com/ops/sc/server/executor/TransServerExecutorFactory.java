package com.ops.sc.server.executor;

import com.ops.sc.common.classloader.ServiceClassNotFoundException;
import com.ops.sc.core.handler.TransEventProcessor;
import com.ops.sc.core.spi.ServerServiceLoader;
import com.ops.sc.core.spi.TransProcessorSPI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransServerExecutorFactory {



    public static TransEventProcessor getRegExecutor(final String modeName) {
         TransProcessorSPI transProcessorSPI = ServerServiceLoader.getCachedProcessorServiceInstance(modeName);
         if(transProcessorSPI instanceof  TransEventProcessor){
             return (TransEventProcessor)transProcessorSPI;
         }
         throw new  ServiceClassNotFoundException("Cannot find executor for trans branch `%s`", modeName);
    }


    public static TransEventProcessor getRespExecutor(final String modeName) {
        TransProcessorSPI transProcessorSPI = ServerServiceLoader.getCachedProcessorServiceInstance(modeName);
        if(transProcessorSPI instanceof  TransEventProcessor){
            return (TransEventProcessor)transProcessorSPI;
        }
        throw new  ServiceClassNotFoundException("Cannot find executor for trans branch`%s`", modeName);
    }
}
