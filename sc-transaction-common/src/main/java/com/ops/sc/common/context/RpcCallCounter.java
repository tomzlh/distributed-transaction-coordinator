
package com.ops.sc.common.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;


public class RpcCallCounter {

    private static final ConcurrentMap<String, RpcCallCounter> SERVICE_COUNTER_MAP = new ConcurrentHashMap<>();
    private final AtomicLong active = new AtomicLong();
    private final LongAdder total = new LongAdder();

    private RpcCallCounter() {
    }

    /**
     * get the RpcStatus of this service
     *
     * @param service the service
     * @return RpcStatus
     */
    public static RpcCallCounter getCounter(String service) {
        return SERVICE_COUNTER_MAP.computeIfAbsent(service, key -> new RpcCallCounter());
    }

    /**
     * remove the RpcStatus of this service
     *
     * @param service the service
     */
    public static void removeCounter(String service) {
        SERVICE_COUNTER_MAP.remove(service);
    }

    /**
     * begin count
     *
     * @param service the service
     */
    public static void beginCount(String service) {
        getCounter(service).active.incrementAndGet();
    }

    /**
     * end count
     *
     * @param service the service
     */
    public static void endCount(String service) {
        RpcCallCounter rpcCallCounter = getCounter(service);
        rpcCallCounter.active.decrementAndGet();
        rpcCallCounter.total.increment();
    }

    /**
     * get active.
     *
     * @return active
     */
    public long getActive() {
        return active.get();
    }

    /**
     * get total.
     *
     * @return total
     */
    public long getTotal() {
        return total.longValue();
    }
}
