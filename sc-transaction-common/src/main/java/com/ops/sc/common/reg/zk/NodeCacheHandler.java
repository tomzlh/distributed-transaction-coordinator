
package com.ops.sc.common.reg.zk;

import org.apache.curator.framework.recipes.cache.NodeCache;


public interface NodeCacheHandler {


    void process(NodeCache nodeCache) throws Exception;
    
}
