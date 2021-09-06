
package com.ops.sc.common.lb;


import com.ops.sc.common.anno.LoadLevel;
import com.ops.sc.common.context.RpcCallCounter;


import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.ops.sc.common.constant.Constants.LEAST_ACTIVE_LOAD_BALANCE;


@LoadLevel(name = LEAST_ACTIVE_LOAD_BALANCE)
public class LeastActiveLoadBalance extends LoadBalance {

    @Override
    protected <T> T chooseWay(List<T> invokers, String xid) {
        int length = invokers.size();
        long leastActive = -1;
        int leastCount = 0;
        int[] leastIndexes = new int[length];
        for (int i = 0; i < length; i++) {
            long active = RpcCallCounter.getCounter(invokers.get(i).toString()).getActive();
            if (leastActive == -1 || active < leastActive) {
                leastActive = active;
                leastCount = 1;
                leastIndexes[0] = i;
            } else if (active == leastActive) {
                leastIndexes[leastCount++] = i;
            }
        }
        if (leastCount == 1) {
            return invokers.get(leastIndexes[0]);
        }
        return invokers.get(leastIndexes[ThreadLocalRandom.current().nextInt(leastCount)]);
    }
}
