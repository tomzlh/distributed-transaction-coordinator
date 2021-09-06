package com.ops.sc.server.glock.redis;

import com.ops.sc.common.model.TransLock;
import com.ops.sc.core.glock.LockStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;


@Component("redisLockStore")
@ConditionalOnProperty(name = "sc.sever.lock.mode", havingValue = "redis")
public class RedisLockStore implements LockStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockStore.class);

    public RedisLockStore() {
        LOGGER.info("********** RedisLock Mode **********");
    }

    /**
     * 释放指定Tid和branchId的锁记录
     *
     * @param tid
     * @param bid
     */
    @Override
    public void deleteByTidAndBranchId(Long tid, Long bid) {
    }

    /**
     * 查询已有的锁记录
     *
     * @param tableName
     * @param kvList
     * @return
     */
    @Override
    public List<TransLock> queryTransLockList(String tableName, List<String> kvList) {
        return null;
    }

    @Override
    public List<TransLock> queryTransLockList(Long tid) {
        return null;
    }

    /**
     * 批量插入锁记录
     *
     * @param lockList
     */
    @Override
    public void batchInsert(List<TransLock> lockList) {
    }
}
