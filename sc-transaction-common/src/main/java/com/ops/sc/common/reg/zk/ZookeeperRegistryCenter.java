
package com.ops.sc.common.reg.zk;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.constant.ServerConstants;
import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.enums.TransactionType;
import com.ops.sc.common.reg.RegConfFactory;
import com.ops.sc.common.utils.InetUtil;
import com.ops.sc.common.config.PropertyResolver;
import com.ops.sc.common.reg.base.RegistryService;
import com.ops.sc.common.reg.base.RegistryType;
import com.ops.sc.common.reg.exception.RegExceptionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;


@Slf4j
public final class ZookeeperRegistryCenter implements RegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistryCenter.class);


    private final Map<String, PathChildrenCache> pathCacheMap = new ConcurrentHashMap<String, PathChildrenCache>();
    private final Map<String, NodeCache> nodeCacheMap = new ConcurrentHashMap<String, NodeCache>();
    private final Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<String, TreeCache>();

    private final Map<String, ExecutorService> pathCacheExecutor = new ConcurrentHashMap<String, ExecutorService>();
    private final Map<String, ExecutorService> nodeCacheExecutor = new ConcurrentHashMap<String, ExecutorService>();
    private final Map<String, ExecutorService> treeCacheExecutor = new ConcurrentHashMap<String, ExecutorService>();
    
    @Getter
    private  CuratorFramework client;

    private static volatile ZookeeperRegistryCenter instance;

    private static final String ZK_PATH_SPLIT_CHAR = "/";
    private static final String FILE_ROOT_REGISTRY = "sc";
    private static final String FILE_CONFIG_SPLIT_CHAR = ".";
    private static final String REGISTRY_CLUSTER = "cluster";
    private static final String REGISTRY_TYPE = "registry";
    private static final String SERVER_ADDR_KEY = "serverAddress";
    private static final String AUTH_USERNAME = "username";
    private static final String AUTH_PASSWORD = "password";
    private static final String SESSION_TIME_OUT_KEY = "sessionTimeout";
    private static final String CONNECT_TIME_OUT_KEY = "connectTimeout";
    private static final String BASE_SLEEP_MS = "baseSleepMs";
    private static final String MAX_SLEEP_MS = "maxSleepMs";
    private static final String MAX_RETRIES = "maxRetries";
    private static final String NAMESPACE = "service-coordinator";
    private static final String DIGEST = "digest";
    public static final String PREFIX = "/sc/grpc/";
    public static final String LOCAL_MODE_SUFFIX = "local";

    private static final int DEFAULT_SESSION_TIMEOUT = 60000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 2000;
    private static final String FILE_CONFIG_KEY_PREFIX = FILE_ROOT_REGISTRY + FILE_CONFIG_SPLIT_CHAR + REGISTRY_TYPE
            + FILE_CONFIG_SPLIT_CHAR;
    private static final String ROOT_PATH = ZK_PATH_SPLIT_CHAR + FILE_ROOT_REGISTRY + ZK_PATH_SPLIT_CHAR + REGISTRY_TYPE
            + ZK_PATH_SPLIT_CHAR;
    private static final String ROOT_PATH_WITHOUT_SUFFIX = ZK_PATH_SPLIT_CHAR + FILE_ROOT_REGISTRY + ZK_PATH_SPLIT_CHAR
            + REGISTRY_TYPE;
    private static final Set<String> REGISTERED_PATH_SET = Collections.synchronizedSet(new HashSet<>(1));

    private static final ConcurrentMap<String, List<String>> CLUSTER_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, PathChildrenCacheListener> LISTENER_SERVICE_MAP = new ConcurrentHashMap<>();


    public static ZookeeperRegistryCenter getInstance() {
        if (instance == null) {
            synchronized (ZookeeperRegistryCenter.class) {
                if (instance == null) {
                    instance = new ZookeeperRegistryCenter();
                }
            }
        }
        return instance;
    }
    
    @Override
    public void init() {
        log.debug("sc: zookeeper registry center init, server lists is: {}.", PropertyResolver.getINSTANCE().getValue(FILE_CONFIG_KEY_PREFIX + SERVER_ADDR_KEY));
        int baseSleepTime=PropertyResolver.getINSTANCE().getIntValue(FILE_CONFIG_KEY_PREFIX + BASE_SLEEP_MS,2);
        int maxSleepTime=PropertyResolver.getINSTANCE().getIntValue(FILE_CONFIG_KEY_PREFIX + MAX_SLEEP_MS,10);
        int sessionTimeOut=PropertyResolver.getINSTANCE().getIntValue(FILE_CONFIG_KEY_PREFIX + SESSION_TIME_OUT_KEY,DEFAULT_SESSION_TIMEOUT);
        int connectTimeOut=PropertyResolver.getINSTANCE().getIntValue(FILE_CONFIG_KEY_PREFIX + CONNECT_TIME_OUT_KEY,DEFAULT_CONNECT_TIMEOUT);
        int maxRetries=PropertyResolver.getINSTANCE().getIntValue(FILE_CONFIG_KEY_PREFIX + MAX_RETRIES,1);
        String digest=PropertyResolver.getINSTANCE().getValue(FILE_CONFIG_KEY_PREFIX + DIGEST);

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(PropertyResolver.getINSTANCE().getValue(FILE_CONFIG_KEY_PREFIX + SERVER_ADDR_KEY))
                .retryPolicy(new ExponentialBackoffRetry(baseSleepTime,maxRetries,maxSleepTime))
                .namespace(NAMESPACE);
       if (0 != sessionTimeOut) {
            builder.sessionTimeoutMs(sessionTimeOut);
        }
        if (0 != connectTimeOut) {
            builder.connectionTimeoutMs(connectTimeOut);
        }
        if (!Strings.isNullOrEmpty(digest)) {
            builder.authorization("digest", digest.getBytes(StandardCharsets.UTF_8))
                    .aclProvider(new ACLProvider() {
                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                        @Override
                        public List<ACL> getAclForPath(final String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }
        client = builder.build();
        try {
            client.start();
            if (!client.blockUntilConnected(maxSleepTime * maxRetries, TimeUnit.MILLISECONDS)) {
                client.close();
                throw new KeeperException.OperationTimeoutException();
            }
            LOGGER.info("success connect to Zookeeper: " + PropertyResolver.getINSTANCE().getValue(FILE_CONFIG_KEY_PREFIX + SERVER_ADDR_KEY));
            // 应用关闭时，主动释放资源
            //shutdownHook();
        } catch (Exception ex) {
            LOGGER.error("start curator client error!", ex);
            if (client != null) {
                close();
            }
        }
        register(RegistryType.ZK,this);
    }


    @Override
    public void register(RegistryType registryType, RegistryService registryService) {
        RegConfFactory.getInstance().registerRegistry(registryType,registryService);
    }

    @Override
    public void register(String branchName, TransMode transMode, int type, String url, String rpcType) {
        if (url == null) {
            throw new IllegalArgumentException("invalid url!");
        }
        if(transMode==TransMode.SAGA) {
            if (type == Constants.COMMIT){
                registerUrl(branchName, url, rpcType, ServerConstants.HttpAction.SAGA_COMMIT_NAME);

            }
            else {
                registerUrl(branchName, url, rpcType, ServerConstants.HttpAction.SAGA_ROLLBACK_NAME);
            }
        }
        else if(transMode==TransMode.TCC){
            if(type == Constants.PREPARE){
                registerUrl(branchName, url, rpcType, ServerConstants.HttpAction.TCC_PREPARE_NAME);
            }
            else if (type == Constants.COMMIT) {
                registerUrl(branchName, url, rpcType, ServerConstants.HttpAction.TCC_COMMIT_NAME);
            }
           else{
                registerUrl(branchName, url, rpcType, ServerConstants.HttpAction.TCC_ROLLBACK_NAME);
            }
        }
        else if(transMode==TransMode.XA){
            if(type == Constants.PREPARE){
                registerUrl(branchName, url, rpcType, ServerConstants.HttpAction.XA_PREPARE_NAME);
            }
            else if (type == Constants.COMMIT) {
                registerUrl(branchName, url, rpcType, ServerConstants.HttpAction.XA_COMMIT_NAME);
            }
            else{
                registerUrl(branchName, url, rpcType, ServerConstants.HttpAction.XA_ROLLBACK_NAME);
            }
        }
    }

    private void registerUrl(String branchName, String url, String rpcType, String methodName) {
        String parentPath = ROOT_PATH + rpcType+ZK_PATH_SPLIT_CHAR+branchName;
        if(url.contains("/")){
            url=url.replaceAll("/","@");
            url=methodName+"#"+url;
        }
        String path = getRegisterPathByPath(parentPath, url);
        doRegister(parentPath, path, url);
    }




    @Override
    public void register(InetSocketAddress address,String rpcType) {
        if (address.getHostName() == null || 0 == address.getPort()) {
            throw new IllegalArgumentException("invalid address:" + address);
        }
        String parentPath=ROOT_PATH + rpcType +ZK_PATH_SPLIT_CHAR+ getClusterName();
        String path = getRegisterPathByPath(parentPath,address);
        doRegister(parentPath,path,InetUtil.toStringAddress(address));
    }

    private boolean doRegister(String parentPath,String path,String url) {
        if (isExisted(path)) {
            return false;
        }
        try {
            if(!isExisted(parentPath)){
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(parentPath);
            }
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path,url.getBytes(Charset.defaultCharset()));
            SessionConnectionListener sessionConnectionListener = new SessionConnectionListener(path, url);
            client.getConnectionStateListenable().addListener(sessionConnectionListener);
            REGISTERED_PATH_SET.add(path);
        }catch (Exception e){
            RegExceptionHandler.handleException(e);
        }
        return true;
    }


    @Override
    public void unregister(InetSocketAddress address) {
        try{
            if (address.getHostName() == null || 0 == address.getPort()) {
                throw new IllegalArgumentException("invalid address:" + address);
            }
            String parentPath=ROOT_PATH + GPRC +ZK_PATH_SPLIT_CHAR+ getClusterName() + ZK_PATH_SPLIT_CHAR;
            String path = getRegisterPathByPath(parentPath,address);
            client.delete().deletingChildrenIfNeeded().forPath(path);
            REGISTERED_PATH_SET.remove(path);
        }catch(Exception e){
            RegExceptionHandler.handleException(e);
        }
    }

    @Override
    public boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
            return false;
        }
    }

    @Override
    public boolean subscribe(String clusterName,String path)  {
        if (clusterName == null) {
            return false;
        }
        try {
          if (!isExisted(path)) {
             client.create().withMode(CreateMode.PERSISTENT).forPath(path);
          }
          this.createPathCache(path,event -> {
              if(event.getData()==null){
                  return;
              }
              byte[] data = event.getData().getData();
              String dataStr = new String(data,StandardCharsets.UTF_8);
              List<String> inetSocketAddressList=CLUSTER_ADDRESS_MAP.get(clusterName);
              if(inetSocketAddressList==null){
                  inetSocketAddressList=new ArrayList<>();
                  CLUSTER_ADDRESS_MAP.put(clusterName,inetSocketAddressList);
              }
              switch (event.getType()) {
                  case CHILD_ADDED: {
                      LOGGER.debug("Path Node added: " + path + " data " + dataStr);
                      makeInetSocketAddress(inetSocketAddressList,dataStr);
                      CLUSTER_ADDRESS_MAP.put(clusterName, inetSocketAddressList);
                      break;
                  }
                  case CHILD_UPDATED: {
                      LOGGER.debug("Path Node changed: " + path + " data " + dataStr);
                      int index= getIndexFromInetSocketAddress(inetSocketAddressList,dataStr);
                      if(index>=0) {
                          inetSocketAddressList.remove(index);
                          makeInetSocketAddress(inetSocketAddressList,dataStr);
                          CLUSTER_ADDRESS_MAP.put(clusterName, inetSocketAddressList);
                      }
                      break;
                  }
                  case CHILD_REMOVED: {
                      LOGGER.debug("Path Node deleted: " + path + " data " + dataStr);
                      int index= getIndexFromInetSocketAddress(inetSocketAddressList,dataStr);
                      if(index>=0) {
                          inetSocketAddressList.remove(index);
                          CLUSTER_ADDRESS_MAP.put(clusterName, inetSocketAddressList);
                      }
                      break;
                  }
              }
          });
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
            return false;
        }
        return true;
    }


    @Override
    public boolean unsubscribe(String clusterName,String path) {
        if (clusterName == null) {
            return false;
        }
        try {
            CLUSTER_ADDRESS_MAP.remove(clusterName);
            if (isExisted(path)) {
                closePathCache(path);
            }
        }catch (Exception e){
            RegExceptionHandler.handleException(e);
            return false;
        }
        return true;
    }


    @Override
    public List<String> lookup(String clusterName,String rpcType) {
        if (clusterName == null) {
            return null;
        }
        return doLookup(clusterName,rpcType);
    }


    List<String> doLookup(String clusterName,String rpcType) {
        String path =ROOT_PATH +rpcType +ZK_PATH_SPLIT_CHAR+ clusterName;
        boolean exist = isExisted(path);
        if (!exist) {
            return null;
        }
        try {
            if (!LISTENER_SERVICE_MAP.containsKey(path)) {
                List<String> childClusterPath = client.getChildren().forPath(path);
                refreshClusterAddressMap(clusterName, childClusterPath);
                subscribeCluster(clusterName,path);
            }
        }catch (Exception e){
            RegExceptionHandler.handleException(e);
            return null;
        }
        return CLUSTER_ADDRESS_MAP.get(clusterName);
    }

    private void subscribeCluster(String cluster,String path){
        subscribe(cluster,path);
    }

    private static void refreshClusterAddressMap(String clusterName, List<String> instances) {
        List<String> newAddressList = new ArrayList<>();
        if (instances == null) {
            CLUSTER_ADDRESS_MAP.put(clusterName, newAddressList);
            return;
        }
        for (String path : instances) {
            makeInetSocketAddress(newAddressList, path);
        }
        CLUSTER_ADDRESS_MAP.put(clusterName, newAddressList);
    }

    private static void makeInetSocketAddress(List<String> newAddressList, String path) {
        try {
            newAddressList.add(path);
        } catch (Exception e) {
            LOGGER.warn("The cluster instance info is error, instance info:{}", path);
        }
    }

    private static int getIndexFromInetSocketAddress(List<String> newAddressList, String path) {
        try {
            for(int i=0;i<newAddressList.size();i++) {
                String inetSocketAddress=newAddressList.get(i);
                if(inetSocketAddress.equals(path)){
                    return i;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("The cluster instance info is error, instance info:{}", path);
        }
        return -1;
    }

    @Override
    public void close() {
        waitForCacheClose();
        CloseableUtils.closeQuietly(client);
    }


    private void waitForCacheClose() {
        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public String get(final String path) {
        String response = null;
        if (!isExisted(path)) {
            return response;
        }
        try {
            byte[] datas = client.getData().forPath(path);
            response = datas == null ? "" : new String(datas, "utf-8");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("读取数据成功, path:" + path + ", content:" + response);
            }
        } catch (Exception e) {
            LOGGER.error("getData，读取数据失败! path: " + path + ", errMsg:" + e.getMessage(), e);
        }
        return response;
    }

    @Override
    public List<String> getChildren(String path) {
        List<String> list = null;
        if (!isExisted(path)) {
            return list;
        }
        try {
            list = client.getChildren().forPath(path);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getChildren，读取数据成功, path:" + path);
            }
        } catch (Exception e) {
            LOGGER.error("getChildren，读取数据失败! path: " + path + ", errMsg:" + e.getMessage(), e);
        }
        return list;
    }


    @Override
    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            } else {
                update(key, value);
            }
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public boolean update(final String key, final String value) {
        try {
            Stat stat = client.setData().forPath(key, value.getBytes(StandardCharsets.UTF_8));
            LOGGER.info("setData，更新数据成功, path:" + key + ", stat: " + stat);
            return true;
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
            return false;
        }
    }

    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public String persistSequential(final String key, final String value) {
        try {
            return client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        }
        return null;
    }

    
    @Override
    public String getDirectly(final String key) {
        try {
            return new String(client.getData().forPath(key), StandardCharsets.UTF_8);
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
            return null;
        }
    }

    @Override
    public Object getRawClient() {
        return client;
    }


    private String getRegisterPathByPath(String parentPath,String url) {
        return  parentPath + ZK_PATH_SPLIT_CHAR + url;
    }

    private String getRegisterPathByPath(String parentPath,InetSocketAddress address) {
        return  parentPath + ZK_PATH_SPLIT_CHAR + InetUtil.toStringAddress(address);
    }

    public String getClusterName() {
        String clusterConfigName = String.join(FILE_CONFIG_SPLIT_CHAR, FILE_ROOT_REGISTRY, REGISTRY_TYPE, REGISTRY_CLUSTER);
        String clusterName = PropertyResolver.getINSTANCE().getValue(clusterConfigName);
        return clusterName;
    }

    @Override
    public PathChildrenCache createPathCache(String path, PathCacheHandler handler) throws Exception {
        PathChildrenCache childrenCache = new PathChildrenCache(this.client, path, true);
        PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                handler.process(event);
            }
        };
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-PathChildrenCacheListener-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(1, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        childrenCache.getListenable().addListener(childrenCacheListener, pool);
        LISTENER_SERVICE_MAP.put(path,childrenCacheListener);
        PathChildrenCache current = pathCacheMap.putIfAbsent(path, childrenCache);
        if (current == null) {
            pathCacheExecutor.putIfAbsent(path, pool);
            childrenCache.start();
            LOGGER.info("Register zookeeper path: [" + path + "]'s PathChildrenCache successfully!");
            return childrenCache;
        }
        // 资源早已存在，关闭无效的资源
        LOGGER.info("zookeeper path: [" + path + "]'s PathChildrenCache already exists!");
        childrenCache.close();
        pool.shutdown();
        return current;
    }

    @Override
    public NodeCache createNodeCache(String path, NodeCacheHandler handler) throws Exception {
        NodeCache nodeCache = new NodeCache(this.client, path, false);
        NodeCacheListener nodeListener = new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                handler.process(nodeCache);
            }
        };
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-NodeCacheListener-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(1, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        nodeCache.getListenable().addListener(nodeListener, pool);
        NodeCache current = nodeCacheMap.putIfAbsent(path, nodeCache);
        if (current == null) {
            nodeCacheExecutor.putIfAbsent(path, pool);
            nodeCache.start();
            LOGGER.info("Register zookeeper path: [" + path + "]'s NodeCache successfully!");
            return nodeCache;
        }
        // 资源早已存在，关闭无效的资源
        LOGGER.info("zookeeper path: [" + path + "]'s NodeCache already exists!");
        nodeCache.close();
        pool.shutdown();
        return current;
    }

    @Override
    public TreeCache createTreeCache(String path, TreeCacheHandler handler) throws Exception {
        TreeCache treeCache = new TreeCache(this.client, path);
        TreeCacheListener treeCacheListener = new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                handler.process(event);
            }
        };
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-TreeCacheListener-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(1, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        treeCache.getListenable().addListener(treeCacheListener, pool);
        TreeCache current = treeCacheMap.putIfAbsent(path, treeCache);
        if (current == null) {
            treeCacheExecutor.putIfAbsent(path, pool);
            treeCache.start();
            LOGGER.info("Register zookeeper path: [" + path + "]'s TreeCache successfully!");
            return treeCache;
        }
        // 资源早已存在，关闭无效的资源
        LOGGER.info("zookeeper path: [" + path + "]'s TreeCache already exists!");
        treeCache.close();
        pool.shutdown();
        return current;
    }

    @Override
    public void closePathCache(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        LISTENER_SERVICE_MAP.remove(path);
        PathChildrenCache pathCache = pathCacheMap.get(path);
        if (pathCache != null) {
            pathCacheMap.remove(path);
            pathCache.close();
            LOGGER.info("close PathChildrenCache:" + path);
        }
        ExecutorService executor = pathCacheExecutor.get(path);
        if (executor != null) {
            pathCacheExecutor.remove(path);
            executor.shutdown();
            LOGGER.info("close ExecutorService for PathChildrenCache:" + path);
        }
    }

    @Override
    public void closeNodeCache(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        NodeCache nodeCache = nodeCacheMap.get(path);
        if (nodeCache != null) {
            nodeCacheMap.remove(path);
            nodeCache.close();
            LOGGER.info("close NodeCache:" + path);
        }
        ExecutorService executor = nodeCacheExecutor.get(path);
        if (executor != null) {
            nodeCacheExecutor.remove(path);
            executor.shutdown();
            LOGGER.info("close ExecutorService for NodeCache:" + path);
        }
    }

    @Override
    public void closeTreeCache(String path){
        if (StringUtils.isEmpty(path)) {
            return;
        }
        TreeCache treeCache = treeCacheMap.get(path);
        if (treeCache != null) {
            treeCacheMap.remove(path);
            treeCache.close();
            LOGGER.info("close TreeCache:" + path);
        }
        ExecutorService executor = treeCacheExecutor.get(path);
        if (executor != null) {
            treeCacheExecutor.remove(path);
            executor.shutdown();
            LOGGER.info("close ExecutorService for TreeCache:" + path);
        }
    }

    @Override
    public void closeAllPathCache() throws Exception {

        Set<String> paths = pathCacheMap.keySet();
        for (String path : paths) {
            closePathCache(path);
        }
    }

    @Override
    public void closeAllNodeCache() throws Exception {
        Set<String> paths = nodeCacheMap.keySet();
        for (String path : paths) {
            closeNodeCache(path);
        }
    }

    @Override
    public void closeAllTreeCache() throws Exception {
        Set<String> paths = treeCacheMap.keySet();
        for (String path : paths) {
            closeTreeCache(path);
        }
    }


    @Override
    public void closeAll() {
        try {
            closeAllPathCache();
            closeAllNodeCache();
            closeAllTreeCache();
            close();
        }catch (Exception ex){
            RegExceptionHandler.handleException(ex);
        }
    }


    /*private void shutdownHook() {
        LOGGER.info("addShutdownHook for CuratorClient");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOGGER.info("shutdownHook begin");
                    closeAll();
                    LOGGER.info("shutdownHook end");
                } catch (Exception e) {
                    LOGGER.error("shutdownHook error", e);
                }
            }
        }));
    }*/

}
