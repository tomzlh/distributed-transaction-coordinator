package com.ops.sc.common.reg.etcd;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.ops.sc.common.constant.EtcdConstants;

import com.ops.sc.common.exception.RequestException;
import com.ops.sc.common.heartbeat.HeartBeatStatusPublisher;
import com.ops.sc.common.heartbeat.HeartBeatStatusSubscriber;
import com.ops.sc.common.reg.RegConfFactory;
import com.ops.sc.common.reg.base.RegistryService;
import com.ops.sc.common.reg.base.RegistryType;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseTimeToLiveResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.lock.UnlockResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.LeaseOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

import static io.netty.util.CharsetUtil.UTF_8;


public class EtcdServiceImpl implements EtcdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdServiceImpl.class);
    /**
     * interval for life keep
     */
    private static final long LIFE_KEEP_INTERVAL = 5;
    /**
     * critical value for life keep
     */
    private static final long LIFE_KEEP_CRITICAL = 6;
    private static final int THREAD_POOL_SIZE = 2;
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE,
            Integer.MAX_VALUE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    /**
     * TTL for lease
     */
    private static final long TTL = 10;
    /**
     * 利用ETCD的租约续期功能模拟zk的临时节点
     */
    private static final Map<String, Long> LEASE_ALIVE_KEEPER_MAP = Maps.newConcurrentMap();
    /**
     * 保存Put时Key和leaseId的对应关系，便于后续续约key
     */
    private static final Map<String, Long> LEASE_ALIVE_ONCE_MAP = Maps.newConcurrentMap();
    /**
     * DIAL_TIMEOUT
     */
    private static final Long DIAL_TIMEOUT = 5L;
    /**
     * 维持ETCD连接使用
     */
    private static final String LIFE_KEEPER_KEY = "/sc/life/keeper";
    private static volatile Client client;

    @Value("${sc.etcd.address}")
    private String etcdAddress;

    @Value("${jetcd.pool.corePoolSize}")
    private Integer jetcdTaskCorePoolSize;

    @Value("${jetcd.pool.maxPoolSize}")
    private Integer jetcdTaskMaxPoolSize;

    @Value("${jetcd.pool.keepAliveSeconds}")
    private Integer jetcdTaskKeepAliveSeconds;

    @Value("${jetcd.pool.queueCapacity}")
    private Integer jetcdTaskQueueCapacity;

    @Resource
    private HeartBeatStatusSubscriber heartBeatStatusSubscriber;

    private ExecutorService executorService;



    private Client getClient() {
        if (client == null) {
            synchronized (EtcdServiceImpl.class) {
                if (client == null) {
                    try {
                        client = Client.builder().executorService(executorService).endpoints(etcdAddress.split(","))
                                .build();
                        Optional<String> member = client.getClusterClient().listMember().get().getMembers().stream()
                                .map(m -> String.format("%s ", m.getName())).reduce(String::concat);
                        LOGGER.info("Connected to etcd server: {}, name list: {}", etcdAddress, member.orElse("Empty"));
                        long lifeKeeperLeaseId = client.getLeaseClient().grant(TTL).get().getID();
                        LEASE_ALIVE_KEEPER_MAP.put(LIFE_KEEPER_KEY, lifeKeeperLeaseId);
                        LOGGER.info("Create a new lease: {} for {}", lifeKeeperLeaseId, LIFE_KEEPER_KEY);
                    } catch (InterruptedException | ExecutionException
                            | io.etcd.jetcd.common.exception.EtcdException e) {
                        LOGGER.error("Create lease error", e);
                        throw new EtcdException(e);
                    }
                    EtcdLifeKeeper lifeKeeper = new EtcdLifeKeeper();
                    EXECUTOR_SERVICE.submit(lifeKeeper);
                }
            }
        }
        return client;
    }

    private ByteSequence buildAppNameKeyPrefix() {
        return ByteSequence.from(EtcdConstants.PREFIX, UTF_8);
    }

    private KV getKVClient() {
        return getClient().getKVClient();
    }

    private Lock getLockClient() {
        return getClient().getLockClient();
    }

    private Lease getLeaseClient() {
        return getClient().getLeaseClient();
    }

    @Override
    public void put(String key, String value) {
        put(key, value, 0);
    }

    /**
     * Put key value into etcd with ttl
     *
     * @param key
     * @param value
     * @param ttl
     */
    @Override
    public void put(String key, String value, long ttl) {

        if (StringUtils.isBlank(key) || StringUtils.isBlank(value) || ttl < 0) {
            throw new IllegalArgumentException("Put parameter is not correct");
        }

        ByteSequence keyUrl = ByteSequence.from(key, Charsets.UTF_8);
        ByteSequence val = ByteSequence.from(value, Charsets.UTF_8);

        CompletableFuture<PutResponse> future;
        long leaseId = 0;
        if (ttl > 0) {
            try {
                leaseId = getLeaseClient().grant(ttl).get(DIAL_TIMEOUT, TimeUnit.SECONDS).getID();
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new EtcdException(ex);
            }
        }

        PutOption option = leaseId > 0 ? PutOption.newBuilder().withLeaseId(leaseId).build() : PutOption.DEFAULT;

        future = getKVClient().put(keyUrl, val, option);

        try {
            future.get(DIAL_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            throw new EtcdException(ex);
        }

        if (leaseId > 0) {
            LEASE_ALIVE_ONCE_MAP.put(key, leaseId);
        }
    }

    /**
     * ETCD使用Key的前缀来模拟zk的目录结构
     *
     * - prefix相同的key底层顺序存储，因此查询速度较快(B-Tree)
     *
     * @param keyPrefix
     * @return
     */
    @Override
    public Map<String, String> getAllValueByPrefix(String keyPrefix) {
        if (StringUtils.isBlank(keyPrefix)) {
            throw new IllegalArgumentException("Get parameter is not correct");
        }
        Map<String, String> result = Maps.newHashMap();
        ByteSequence prefix = ByteSequence.from(keyPrefix, Charsets.UTF_8);
        // 优化：可以只返回Keys,减少数据传输量
        GetOption getOption = GetOption.newBuilder().withPrefix(prefix).build();
        CompletableFuture<GetResponse> future = getKVClient().get(prefix, getOption);
        try {
            GetResponse resp = future.get(DIAL_TIMEOUT, TimeUnit.SECONDS);
            if (resp.getCount() == 0) {
                return result;
            }
            resp.getKvs().forEach((keyValue -> result.put(keyValue.getKey().toString(Charsets.UTF_8),
                    keyValue.getValue().toString(Charsets.UTF_8))));
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            LOGGER.error("Get error when retrieve for {}, err {}", keyPrefix, ex);
        }
        return result;
    }

    /**
     * delete some key
     *
     * @param key
     */
    @Override
    public void delete(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Delete parameter is not correct");
        }

        LEASE_ALIVE_ONCE_MAP.remove(key);

        ByteSequence keyUrl = ByteSequence.from(key, Charsets.UTF_8);
        CompletableFuture<DeleteResponse> deleteFuture = getKVClient().delete(keyUrl);
        try {
            deleteFuture.get(DIAL_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            LOGGER.error("Delete error when retrieve for {}, err {}", key, ex);
        }
    }

    /**
     * 续约Key
     *
     * @param key
     */
    @Override
    public void keepAliveOnce(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("KeepAliveOnce parameter is not correct");
        }
        if (!LEASE_ALIVE_ONCE_MAP.containsKey(key)) {
            throw new EtcdException();
        }
        long leaseId = LEASE_ALIVE_ONCE_MAP.get(key);
        try {
            getLeaseClient().keepAliveOnce(leaseId).get(DIAL_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            LOGGER.error("KeepAliveOnce error when retrieve for {}, err {}", key, ex);
        }
    }

    /**
     * Get value with key
     *
     * @param key
     * @return
     */
    @Override
    public Optional<String> get(String key) {

        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Get parameter is not correct");
        }

        ByteSequence keyUrl = ByteSequence.from(key, Charsets.UTF_8);
        CompletableFuture<GetResponse> future = getKVClient().get(keyUrl);
        try {
            GetResponse resp = future.get(DIAL_TIMEOUT, TimeUnit.SECONDS);
            if (resp.getCount() == 0) {
                return Optional.empty();
            }
            String value = resp.getKvs().get(0).getValue().toString(Charsets.UTF_8);
            LOGGER.debug("Get kv from {}, value {}", key, value);
            return Optional.of(value);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            LOGGER.error("Get error when retrieve for {}, err {}", key, ex);
        }
        return Optional.empty();
    }

    /**
     *  使用ETCD原生的lock命令获取锁，成功后定时刷新租约，保证调用unlock前key不会删除
     *
     * ETCD服务端会根据设定的租约定期删除未续约的key
     */
    @Override
    public Optional<LockResponse> lock(String lockKey, long ttl) {
        try {
            ByteSequence name = ByteSequence.from(lockKey, Charsets.UTF_8);
            long leaseId = getLeaseClient().grant(ttl).get(DIAL_TIMEOUT, TimeUnit.SECONDS).getID();
            CompletableFuture<LockResponse> completableFuture = getLockClient().lock(name, leaseId);
            LockResponse lockResp = completableFuture.get(DIAL_TIMEOUT, TimeUnit.SECONDS);
            LEASE_ALIVE_KEEPER_MAP.put(lockKey, leaseId);
            LOGGER.debug("Lock for {} success, key: {}", lockKey, lockResp.getKey().toString(Charsets.UTF_8));
            return Optional.of(lockResp);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return Optional.empty();
        }
    }

    /**
     * Release the lock identified by the given key.
     *
     * @param lockKey
     *            key is the lock ownership key granted by Lock.
     */
    @Override
    public Optional<UnlockResponse> unlock(String lockKey) {
        LEASE_ALIVE_KEEPER_MAP.remove(lockKey);
        ByteSequence name = ByteSequence.from(lockKey, Charsets.UTF_8);
        CompletableFuture<UnlockResponse> completableFuture = getLockClient().unlock(name);
        try {
            UnlockResponse unlockResp = completableFuture.get(DIAL_TIMEOUT, TimeUnit.SECONDS);
            LOGGER.debug("Unlock for {} success", lockKey);
            return Optional.of(unlockResp);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Unlock {} fail , err {}", lockKey, e);
            return Optional.empty();
        }
    }

    @Override
    public void register(RegistryType registryType, RegistryService registryService) {
        RegConfFactory.getInstance().registerRegistry(registryType,registryService);
    }

    @PostConstruct
    public void init() {

        LOGGER.info("Start init Etcd...");

        executorService = new ThreadPoolExecutor(jetcdTaskCorePoolSize, jetcdTaskMaxPoolSize,
                jetcdTaskKeepAliveSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<>(jetcdTaskQueueCapacity), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                throw new RequestException(r.toString());
            }
        });


        EtcdWatcher etcdWatcher = new EtcdWatcher(new Watch.Listener() {

            @Override
            public void onNext(WatchResponse response) {
                response.getEvents().forEach(watchEvent -> {
                    if (watchEvent.getEventType() == WatchEvent.EventType.DELETE) {
                        String key = watchEvent.getKeyValue().getKey().toString(Charsets.UTF_8);
                        LOGGER.debug("Receive delete event for {}", key);
                        LEASE_ALIVE_ONCE_MAP.remove(key);
                        // 每一个work线程拥有自己的heartBeatStatusPublisher对象,而heartBeatStatusSubscriber不存在线程安全问题
                        HeartBeatStatusPublisher heartBeatStatusPublisher = new HeartBeatStatusPublisher();
                        heartBeatStatusPublisher.addObserver(heartBeatStatusSubscriber);
                        heartBeatStatusPublisher.setKey(key);
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("EtcdWatcher onError", throwable);
            }

            @Override
            public void onCompleted() {
                LOGGER.debug("EtcdWatcher onCompleted");
            }
        });
        EXECUTOR_SERVICE.submit(etcdWatcher);
    }

    /**
     * the type etcd watcher
     */
    private class EtcdWatcher implements Runnable {
        private final Watch.Listener listener;
        private Watch.Watcher watcher;

        EtcdWatcher(Watch.Listener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            Watch watchClient = getClient().getWatchClient();
            WatchOption watchOption = WatchOption.newBuilder().withPrefix(buildAppNameKeyPrefix()).build();
            this.watcher = watchClient.watch(buildAppNameKeyPrefix(), watchOption, this.listener);
            LOGGER.info("EtcdWatcher start. keyPrefix: {}", buildAppNameKeyPrefix().toString(Charsets.UTF_8));
        }

        public void stop() {
            this.watcher.close();
        }
    }

    private class EtcdLifeKeeper implements Callable<Boolean> {
        private final Lease leaseClient;
        private boolean running;

        EtcdLifeKeeper() {
            this.leaseClient = getLeaseClient();
            this.running = true;
        }

        /**
         * process
         */
        private void process() {
            for (;;) {
                try {
                    LEASE_ALIVE_KEEPER_MAP.forEach((key, leaseId) -> {
                        try {
                            // 1.get TTL
                            LeaseTimeToLiveResponse leaseTimeToLiveResponse = this.leaseClient
                                    .timeToLive(leaseId, LeaseOption.DEFAULT).get();
                            final long ttl = leaseTimeToLiveResponse.getTTl();
                            if (ttl <= LIFE_KEEP_CRITICAL) {
                                // 2.refresh the TTL
                                this.leaseClient.keepAliveOnce(leaseId).get();
                            }
                        } catch (InterruptedException | ExecutionException ex) {
                            LOGGER.error("Keep Etcd Life error!", ex);
                            throw new RuntimeException("Failed to renewal the lease.");
                        }
                    });
                    TimeUnit.SECONDS.sleep(LIFE_KEEP_INTERVAL);
                    if (!this.running) {
                        break;
                    }
                } catch (Exception e) {
                    LOGGER.error("Keep Etcd Life error!", e);
                    throw new RuntimeException("Failed to renewal the lease.");
                }
            }
        }

        void stop() {
            this.running = false;
        }

        @Override
        public Boolean call() {
            if (this.running) {
                process();
            }
            return this.running;
        }
    }

}