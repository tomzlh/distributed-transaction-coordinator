package com.ops.sc.common.utils;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


public class CacheUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheUtil.class);

    private static final long CACHE_TIMEOUT = 3 * 24 * 3600 * 1000l;

    private static final Integer CACHE_CAPACITY = 2000;
    private static final Integer CONCURRENCY_LEVEL = 10;
    private static final Long MAX_SIZE = 10000L;
    public static Cache<Object, Object> clientCacheBuilder = CacheBuilder.newBuilder().initialCapacity(CACHE_CAPACITY)
            .expireAfterWrite(CACHE_TIMEOUT, TimeUnit.MILLISECONDS).concurrencyLevel(CONCURRENCY_LEVEL)
            .maximumSize(MAX_SIZE).removalListener(entry -> LOGGER.info("Remove cache: {}.", entry.getKey())).build();
}
