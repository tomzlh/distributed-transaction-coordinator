
package com.ops.sc.common.lb;


import com.ops.sc.common.utils.CommonUtils;

import java.util.List;


public abstract class LoadBalance {
    
    public <T> T choose(List<T> invokers, String tid) {
        if (CommonUtils.isEmpty(invokers)) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        return chooseWay(invokers, tid);
    }


    protected abstract <T> T chooseWay(List<T> invokers, String tid);
    
}
