
package com.ops.sc.common.reg.base;


import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.reg.RegConfFactory;
import com.ops.sc.common.reg.zk.NodeCacheHandler;
import com.ops.sc.common.reg.zk.PathCacheHandler;
import com.ops.sc.common.reg.zk.TreeCacheHandler;
import com.ops.sc.common.config.ConfigurationCache;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.TreeCache;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public interface RegistryService<T> {

    /**
     * The constant PREFIX_SERVICE_MAPPING.
     */
    String PREFIX_SERVICE_MAPPING = "vgroup";
    /**
     * The constant PREFIX_SERVICE_ROOT.
     */
    String PREFIX_SERVICE_ROOT = "service";
    /**
     * The constant CONFIG_SPLIT_CHAR.
     */
    String CONFIG_SPLIT_CHAR = ".";

    Set<String> SERVICE_GROUP_NAME = new HashSet<>();


    public static final String GPRC = "grpc";
    public static final String NETTY = "netty";
    public static final String HTTP = "http";


    void init();


    void register(RegistryType registryType, RegistryService registryService);

    void register(String name,  TransMode transMode, int type, String url, String rpcType);

    /**
     * Register.
     *
     * @param address the address
     * @throws Exception the exception
     */
    void register(InetSocketAddress address,String rpcType);

    /**
     * Unregister.
     *
     * @param address the address
     * @throws Exception the exception
     */
    void unregister(InetSocketAddress address);

    /**
     * Subscribe.
     *
     * @param clusterName  the cluster
     * @throws Exception the exception
     */
    boolean subscribe(String clusterName,String path);

    /**
     * Unsubscribe.
     *
     * @param clusterName  the cluster
     * @throws Exception the exception
     */
    boolean unsubscribe(String clusterName,String path) ;


    List<InetSocketAddress> lookup(String key,String rpcType) ;

    /**
     * Close.
     * @throws Exception
     */
    void close() throws Exception;

    /*default String getServiceGroup(String key) {
        key = PREFIX_SERVICE_ROOT + CONFIG_SPLIT_CHAR + PREFIX_SERVICE_MAPPING +CONFIG_SPLIT_CHAR+ key;
        if (!SERVICE_GROUP_NAME.contains(key)) {
            //ConfigurationCache.addConfigListener(key);
            SERVICE_GROUP_NAME.add(key);
        }
        return RegConfFactory.getInstance().getConfigService().getConfig(key);
    }*/

    String getDirectly(final String key);

    boolean isExisted(final String key);

    String get(final String key);

    List<String> getChildren(String path);

    Object getRawClient();

    void persist(final String key, final String value);

    boolean update(final String key, final String value);

    void persistEphemeral(final String key, final String value);

    String persistSequential(final String key, final String value);

    PathChildrenCache createPathCache(String path, PathCacheHandler handler) throws Exception;

    NodeCache createNodeCache(String path, NodeCacheHandler handler) throws Exception;

    TreeCache createTreeCache(String path, TreeCacheHandler handler) throws Exception;

    void closePathCache(String path) throws Exception;

    void closeNodeCache(String path) throws Exception;

    void closeTreeCache(String path);

    void closeAllPathCache() throws Exception;

    void closeAllNodeCache() throws Exception;

    void closeAllTreeCache() throws Exception;

    void closeAll();



}
