package com.ops.sc.common.utils;

import java.util.Map;

import com.google.common.collect.Maps;


public class ThreadCacheKVUtil<K, V> {

    private static final ThreadLocal<ThreadCacheKVUtil> CACHE_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    private static final Integer MAP_CAPACITY = 20;

    private Map<K, V> cache;

    public ThreadCacheKVUtil() {
        this.cache = Maps.newHashMapWithExpectedSize(MAP_CAPACITY);
        setContext(this);
    }

    public static <K, V> ThreadCacheKVUtil<K, V> getInstance(Class<K> k, Class<V> v) {
        return CACHE_CONTEXT_THREAD_LOCAL.get();
    }

    private void setContext(ThreadCacheKVUtil<K, V> context) {
        CACHE_CONTEXT_THREAD_LOCAL.set(context);
    }

    /**
     * 清除缓存
     */
    public void clean() {
        CACHE_CONTEXT_THREAD_LOCAL.remove();
    }

    /**
     * 获取缓存中的值
     *
     * @param k
     * @return
     */
    public V get(K k) {
        return this.cache.get(k);
    }

    /**
     * 向缓存中注入值
     *
     * @param k
     * @param v
     */
    public void load(K k, V v) {
        cache.put(k, v);
    }

}
