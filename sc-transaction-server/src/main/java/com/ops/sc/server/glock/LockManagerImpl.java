package com.ops.sc.server.glock;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ops.sc.core.build.LockContextBuilder;
import com.ops.sc.common.utils.CommonUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.ops.sc.common.bean.LockContext;
import com.ops.sc.common.model.TransLock;
import com.ops.sc.core.glock.LockManager;
import com.ops.sc.core.glock.LockStore;
import com.ops.sc.rpc.dto.BranchTransRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


@Service
public class LockManagerImpl implements LockManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockManagerImpl.class);

    @Resource
    private LockStore lockStore;

    /**
     * @param request
     * @param branchId
     * @return
     */
    @Override
    public boolean acquireLock(BranchTransRequest request, Long branchId) {
        LockContext lockContext = LockContextBuilder.rpcRequestToLockContext(request.getLockContext());
        String resourceId = lockContext.getResourceId();
        Long tid = Long.parseLong(request.getTid());

        boolean acquiredLock = true;
        if (lockContext.getRequireLock()) {
            Map<String, Set<String>> operateSetTableName = lockContext.getOperateMap();
            for (Map.Entry<String, Set<String>> entry : operateSetTableName.entrySet()) {
                if (CollectionUtils.isEmpty(entry.getValue())) {
                    continue;
                }
                if (!getLockFromDB(tid, branchId, entry.getValue(), resourceId, entry.getKey())) {
                    acquiredLock = false;
                    break;
                }
            }
        }

        return acquiredLock;
    }


    @Override
    public void globalReleaseLock(Long tid) {
        Preconditions.checkNotNull(tid, "tid cannot be null!");
        // TODO 如果加了内存锁，这里是需要释放内存锁
        lockStore.deleteByTidAndBranchId(tid, null);
    }

    /**
     * 根据TID找出锁记录
     *
     * @param tid
     * @return
     */
    @Override
    public List<TransLock> queryTransLockList(Long tid) {
        return lockStore.queryTransLockList(tid);
    }


    @Override
    public void branchReleaseLock(Long tid, Long branchId) {
        Preconditions.checkNotNull(tid, "tid cannot be null!");
        Preconditions.checkNotNull(branchId, "branchId cannot be null!");
        // TODO 如果加了内存锁，这里是需要释放内存锁
        lockStore.deleteByTidAndBranchId(tid, branchId);
    }

    /**
     * 基于tid,branchId的同一tableName获取锁
     *
     * @param tid
     * @param branchId
     * @param operateSet
     * @param resourceId
     * @param tableName
     * @return
     */
    private boolean getLockFromDB(Long tid, Long branchId, Set<String> operateSet, String resourceId,
            String tableName) {
        String uniqueTableName = CommonUtils.generateLockKey(resourceId, tableName);
        List<TransLock> transLockList = lockStore.queryTransLockList(uniqueTableName, new ArrayList<>(operateSet));
        // 同一事务下，已经加上锁的记录
        List<String> lockedList = transLockList.stream().filter(scLock -> tid.equals(scLock.getTid()))
                .map(TransLock::getPrimaryKeyValue).collect(Collectors.toList());
        // 该记录已被其它事务加锁
        if (transLockList.size() != lockedList.size()) {
            LOGGER.warn("资源已被锁定，无法再加锁. tid: {},resourceId: {}, tableName: {}, operateSet: {}.", tid, resourceId,
                    tableName, operateSet);
            return false;
        }
        // 已加锁的资源不需要重复加锁
        operateSet.removeAll(lockedList);
        if (operateSet.isEmpty()) {
            return true;
        }

        List<TransLock> lockList = Lists.newArrayList();
        for (String id : operateSet) {
            lockList.add(createLock(tid, branchId, uniqueTableName, id));
        }
        lockStore.batchInsert(lockList);
        return true;
    }

    private TransLock createLock(Long tid, Long branchId, String uniqueTableName, String id) {
        TransLock lock = new TransLock();
        lock.setTid(tid);
        lock.setBranchId(branchId);
        lock.setTableName(uniqueTableName);
        lock.setPrimaryKeyValue(id);
        lock.setCreateTime(System.currentTimeMillis());
        return lock;
    }
}
