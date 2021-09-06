
package com.ops.sc.common.reg.zk;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent;


public interface TreeCacheHandler {


    void process(TreeCacheEvent event) throws Exception;
}
