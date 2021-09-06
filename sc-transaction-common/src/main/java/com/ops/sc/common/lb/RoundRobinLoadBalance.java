package com.ops.sc.common.lb;


import com.ops.sc.common.anno.LoadLevel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ops.sc.common.constant.Constants.ROUND_ROBIN_LOAD_BALANCE;


@LoadLevel(name = ROUND_ROBIN_LOAD_BALANCE)
public class RoundRobinLoadBalance extends LoadBalance {

    private final AtomicInteger sequence = new AtomicInteger();

    @Override
    protected <T> T chooseWay(List<T> invokers, String xid) {
        int length = invokers.size();
        return invokers.get(getPositiveSequence() % length);
    }

    private int getPositiveSequence() {
        while(true) {
            int current = sequence.get();
            int next = current >= Integer.MAX_VALUE ? 0 : current + 1;
            if (sequence.compareAndSet(current, next)) {
                return current;
            }
        }
    }

}
