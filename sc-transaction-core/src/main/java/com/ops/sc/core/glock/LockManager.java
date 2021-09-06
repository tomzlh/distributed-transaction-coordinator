package com.ops.sc.core.glock;

import java.util.List;

import com.ops.sc.common.model.TransLock;
import com.ops.sc.rpc.dto.BranchTransRequest;


public interface LockManager {

    /**
     * 获取锁
     *
     * @param request
     * @param bid
     * @return
     */
    boolean acquireLock(BranchTransRequest request, Long bid);

    /**
     * 根据tid找出锁记录
     *
     * @param tid
     * @return
     */
    List<TransLock> queryTransLockList(Long tid);

    /**
     * 全局释放锁
     *
     * @param tid
     */
    void globalReleaseLock(Long tid);

    /**
     * 分支释放锁
     *
     * @param tid
     * @param bid
     */
    void branchReleaseLock(Long tid, Long bid);

}
