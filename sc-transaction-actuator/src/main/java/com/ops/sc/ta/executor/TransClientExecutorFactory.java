package com.ops.sc.ta.executor;

import com.ops.sc.common.classloader.ServiceClassNotFoundException;
import com.ops.sc.core.spi.ClientServiceLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransClientExecutorFactory {

    public static TccClientExecutor getTccExecutor(final String transactionName) {
        return (TccClientExecutor) ClientServiceLoader.getCachedHandlerInstance(transactionName)
                .orElseThrow(() -> new ServiceClassNotFoundException("Cannot find executor for trans branch  `%s`", transactionName));
    }

    public static XaClientExecutor getXaExecutor(final String transactionName) {
        return (XaClientExecutor) ClientServiceLoader.getCachedHandlerInstance(transactionName)
                .orElseThrow(() -> new ServiceClassNotFoundException("Cannot find executor for trans branch `%s`", transactionName));
    }


}
