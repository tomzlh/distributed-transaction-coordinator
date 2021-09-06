package com.ops.sc.core.glock;

import java.util.List;

import com.ops.sc.common.model.TransLock;


public interface LockStore {

    /**
     * 释放指定tid和branchId的锁记录
     *
     * @param tid
     * @param bid
     */
    void deleteByTidAndBranchId(Long tid, Long bid);

    /**
     * 查询已有的锁记录
     *
     * @param tableName
     * @param kvList
     * @return
     */
    List<TransLock> queryTransLockList(String tableName, List<String> kvList);

    List<TransLock> queryTransLockList(Long tid);

    /**
     * 批量插入锁记录
     *
     * @param lockList
     */
    void batchInsert(List<TransLock> lockList);

}
