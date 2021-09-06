
package com.ops.sc.common.reg.zk;

import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;


public interface PathCacheHandler {


    void process(PathChildrenCacheEvent event) throws Exception;
}
