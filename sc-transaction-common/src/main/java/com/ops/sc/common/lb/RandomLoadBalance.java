
package com.ops.sc.common.lb;


import com.ops.sc.common.anno.LoadLevel;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.ops.sc.common.constant.Constants.RANDOM_LOAD_BALANCE;


@LoadLevel(name = RANDOM_LOAD_BALANCE)
public class RandomLoadBalance extends LoadBalance {

    @Override
    protected <T> T chooseWay(List<T> invokers, String xid) {
        int length = invokers.size();
        return invokers.get(ThreadLocalRandom.current().nextInt(length));
    }
}
