package com.ops.sc.common.lb;


import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.anno.LoadLevel;
import com.ops.sc.common.config.PropertyResolver;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.ops.sc.common.constant.Constants.CONSISTENT_HASH_LOAD_BALANCE;
import static com.ops.sc.common.constant.Constants.LOAD_BALANCE_PREFIX;


@LoadLevel(name = CONSISTENT_HASH_LOAD_BALANCE)
public class ConsistentHashLoadBalance extends LoadBalance {

    /**
     * The constant LOAD_BALANCE_CONSISTENT_HASH_VISUAL_NODES.
     */
    public static final String LOAD_BALANCE_CONSISTENT_HASH_VISUAL_NODES = LOAD_BALANCE_PREFIX + "visualNodes";
    /**
     * The constant VIRTUAL_NODES_NUM.
     */
    private static final int VIRTUAL_NODES_NUM = PropertyResolver.getINSTANCE().getIntValue(LOAD_BALANCE_CONSISTENT_HASH_VISUAL_NODES, Constants.VIRTUAL_NODES_DEFAULT);

    @Override
    protected <T> T chooseWay(List<T> invokers, String xid) {
        return new ConsistentHashSelector<>(invokers, VIRTUAL_NODES_NUM).select(xid);
    }

    private static final class ConsistentHashSelector<T> {

        private final SortedMap<Long, T> virtualInvokers = new TreeMap<>();
        private final HashFunction hashFunction = new MD5Hash();

        ConsistentHashSelector(List<T> invokers, int virtualNodes) {
            for (T invoker : invokers) {
                for (int i = 0; i < virtualNodes; i++) {
                    virtualInvokers.put(hashFunction.hash(invoker.toString() + i), invoker);
                }
            }
        }

        public T select(String objectKey) {
            SortedMap<Long, T> tailMap = virtualInvokers.tailMap(hashFunction.hash(objectKey));
            Long nodeHashVal = tailMap.isEmpty() ? virtualInvokers.firstKey() : tailMap.firstKey();
            return virtualInvokers.get(nodeHashVal);
        }
    }

    private static class MD5Hash implements HashFunction {
        MessageDigest instance;
        public MD5Hash() {
            try {
                instance = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        @Override
        public long hash(String key) {
            instance.reset();
            instance.update(key.getBytes());
            byte[] digest = instance.digest();
            long h = 0;
            for (int i = 0; i < 4; i++) {
                h <<= 8;
                h |= ((int) digest[i]) & 0xFF;
            }
            return h;
        }
    }

    /**
     * Hash String to long value
     */
    public interface HashFunction {
        long hash(String key);
    }
}
