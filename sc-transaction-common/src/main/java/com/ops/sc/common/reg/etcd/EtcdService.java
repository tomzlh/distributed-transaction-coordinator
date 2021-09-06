package com.ops.sc.common.reg.etcd;

import com.ops.sc.common.reg.base.RegistryType;
import com.ops.sc.common.reg.base.RegistryService;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.lock.UnlockResponse;

import java.util.Map;
import java.util.Optional;

public interface EtcdService {

    void register(RegistryType registryType, RegistryService registryService);

    void init();

    /**
     * Put key value into etcd with ttl
     *
     * @param key
     * @param value
     * @param ttl
     *            过期时间，单位s
     */
    void put(String key, String value, long ttl);

    void put(String key, String value);

    /**
     * Get value with key
     *
     * @param key
     * @return
     */
    Optional<String> get(String key);

    /**
     * delete some key
     *
     * @param key
     */
    void delete(String key);

    /**
     * 续约Key
     *
     * @param key
     */
    void keepAliveOnce(String key);

    /**
     * ETCD是K-V架构，没有ZK的目录结构，只能使用Key的前缀来模拟zk的目录结构
     *
     * - prefix相同的key底层顺序存储，因此查询速度较快(B-Tree)
     *
     * @param keyPrefix
     * @return
     */
    Map<String, String> getAllValueByPrefix(String keyPrefix);

    /**
     * Acquire a lock with the given name.
     *
     * @param lockKey
     *            the identifier for the distributed shared lock to be acquired.
     */
    Optional<LockResponse> lock(String lockKey, long ttl);

    /**
     * Release the lock identified by the given key.
     *
     * @param lockKey
     *            key is the lock ownership key granted by Lock.
     */
    Optional<UnlockResponse> unlock(String lockKey);

}
