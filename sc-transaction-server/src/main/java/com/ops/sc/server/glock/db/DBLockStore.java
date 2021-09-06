package com.ops.sc.server.glock.db;

import com.ops.sc.common.model.TransLock;
import com.ops.sc.core.glock.LockStore;
import com.ops.sc.mybatis.mapper.TransLockMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


@Component("dbLockStore")
@ConditionalOnProperty(name = "sc.sever.lock.mode", havingValue = "db", matchIfMissing = true)
public class DBLockStore implements LockStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLockStore.class);

    @Resource
    private TransLockMapper transLockMapper;

    public DBLockStore() {
        LOGGER.info("Initializing SC DB LockStore Mode!");
    }

    /**
     * 释放指定Tid和branchId的锁记录
     *
     * @param tid
     * @param branchId
     */
    @Override
    public void deleteByTidAndBranchId(Long tid, Long branchId) {
        transLockMapper.deleteByTidAndBranchId(tid, branchId);
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
        return transLockMapper.queryTransLockList(tableName, kvList);
    }

    @Override
    public List<TransLock> queryTransLockList(Long tid) {
        return transLockMapper.queryTransLockListByTid(tid);
    }

    /**
     * 批量插入锁记录
     *
     * @param lockList
     */
    @Override
    public void batchInsert(List<TransLock> lockList) {
        transLockMapper.batchInsert(lockList);
    }

}
