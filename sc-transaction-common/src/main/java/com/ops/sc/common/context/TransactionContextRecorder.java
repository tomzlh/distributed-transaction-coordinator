package com.ops.sc.common.context;


import com.google.common.collect.Maps;
import com.ops.sc.common.bean.LockContext;
import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.enums.TransferRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.NamedThreadLocal;

import java.sql.Connection;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;


public class TransactionContextRecorder {

    private static final ThreadLocal<TransContext> TRANSACTION_CONTEXT = new ThreadLocal<>();

    private static final ThreadLocal<TransferRole> ROLE_CONTEXT = new ThreadLocal<>();

    private static final ThreadLocal<TransMode> CURRENT_TRANS_MODE = new ThreadLocal<>();

    private static final ThreadLocal<LockContext> CURRENT_LOCK_CONTEXT = new ThreadLocal<>();

    private static final ThreadLocal<Map<Connection, XAContext>> XA_CONTEXT_INFO = new ThreadLocal<>();

    private static final ThreadLocal<Map<Object, Object>> REGISTER_INFO = new NamedThreadLocal<>(
            "Transaction register info");

    /**
     * 设置事务模式
     *
     * @param transMode
     */
    public static void setCurrentTransMode(TransMode transMode) {
        CURRENT_TRANS_MODE.set(transMode);
    }

    public static boolean isFMTTransaction() {
        return TransMode.FMT.equals(CURRENT_TRANS_MODE.get());
    }

    /**
     * 获取当前线程下的事务上下文
     *
     * @return
     */
    public static TransContext getCurrentTransContext() {
        return TRANSACTION_CONTEXT.get();
    }

    /**
     * 设置或刷新当前线程的事务上下文
     *
     * @param transContext
     */
    public static void setCurrentTransContext(TransContext transContext) {
        TransContext transContextExist = TRANSACTION_CONTEXT.get();
        if (transContextExist != null) {
            if (transContext.getIsTransaction() != null) {
                transContextExist.setIsTransaction(transContext.getIsTransaction());
            }

            if (transContext.getAttachments() != null) {
                transContextExist.setAttachments(transContext.getAttachments());
            }

            if (transContext.getTransStatus() != null) {
                transContextExist.setTransStatus(transContext.getTransStatus());
            }
            if (transContext.getCurrentTransactionId() != null) {
                transContextExist.setCurrentTransactionId(transContext.getCurrentTransactionId());
            }

            if (transContext.getParentId() != null) {
                transContextExist.setParentId(transContext.getParentId());
            }


            if (transContext.getServerAddress() != null) {
                transContextExist.setServerAddress(transContext.getServerAddress());
            }

            TRANSACTION_CONTEXT.set(transContextExist);
        } else {
            TRANSACTION_CONTEXT.set(transContext);
        }
    }

    public static ThreadLocal<Map<Object, Object>> getRegisterContext() {
        return REGISTER_INFO;
    }


    public static void bind(TransContext transContext) {
        setCurrentTransContext(transContext);
    }

    public static TransContext removeCurrentTransContext() {
        TransContext transContext = getCurrentTransContext();
        TRANSACTION_CONTEXT.remove();
        return transContext;
    }

    public static void updateTransStatusCurrentContext(TransStatus transStatus) {
        TransContext transContextExist = TRANSACTION_CONTEXT.get();
        checkNotNull(transContextExist, "No current context to update!");
        checkNotNull(transStatus, "transStatus cannot be null!");
        transContextExist.setTransStatus(transStatus);
        TRANSACTION_CONTEXT.remove();
        TRANSACTION_CONTEXT.set(transContextExist);
    }

    public static void updateIfInTransaction(boolean isInTransaction) {
        TransContext transContextExist = TRANSACTION_CONTEXT.get();
        if(transContextExist==null){
            throw new NullPointerException("No transaction context to update!");
        }
        transContextExist.setIsTransaction(isInTransaction);
        TRANSACTION_CONTEXT.remove();
        TRANSACTION_CONTEXT.set(transContextExist);
    }


    public static void clearAllTransContext() {
        TRANSACTION_CONTEXT.remove();
        ROLE_CONTEXT.remove();
        CURRENT_TRANS_MODE.remove();
        CURRENT_LOCK_CONTEXT.remove();
        XA_CONTEXT_INFO.remove();
    }

    public static Long getTid() {
        return isTidExist() ? getCurrentTransContext().getTid() : null;
    }

    public static Long getParentId() {
        return getCurrentTransContext().getParentId();
    }

    public static Long getCurrentTransactionId() {
        return getCurrentTransContext().getCurrentTransactionId();
    }

    public static String getServerAddress() {
        return getCurrentTransContext().getServerAddress();
    }


    public static String getBranchId() {
        return isBranchIdExist() ? getCurrentTransContext().getBranchId() : StringUtils.EMPTY;
    }


    public static void setRoleContext(TransferRole transferRole) {
        ROLE_CONTEXT.set(transferRole);
    }

    /**
     * 判断是否为参与者
     *
     * @return
     */
    public static Boolean isParticipant() {
        return TransferRole.PARTICIPATOR == ROLE_CONTEXT.get();
    }

    /**
     * 判断是否为发起者
     *
     * @return
     */
    public static Boolean isInitiatorFromAspect() {
        return TransferRole.STARTER == ROLE_CONTEXT.get();
    }

    /**
     * 判断是否tid已存在
     *
     * @return
     */
    public static Boolean isTidExist() {
        return TRANSACTION_CONTEXT.get() != null && TRANSACTION_CONTEXT.get().getTid() != null;
    }

    /**
     * 判断branchId是否存在
     *
     * @return
     */
    public static Boolean isBranchIdExist() {
        return TRANSACTION_CONTEXT.get() != null && TRANSACTION_CONTEXT.get().getBranchId()!= null;
    }

    /**
     * 判断是否处于分布式事务中
     *
     * @return
     */
    public static Boolean isInTransaction() {
        return TRANSACTION_CONTEXT.get() != null && TRANSACTION_CONTEXT.get().getIsTransaction();
    }

    public static LockContext getCurrentLockContext() {
        return CURRENT_LOCK_CONTEXT.get();
    }

    public static void clearCurrentLockContext() {
        CURRENT_LOCK_CONTEXT.remove();
    }

    public static void refreshCurrentLockContext(String resourceId, String tableName, Set<String> operateSet,
            boolean requireLock) {
        if (!requireLock || operateSet.isEmpty()) {
            CURRENT_LOCK_CONTEXT.set(new LockContext(resourceId, Maps.newHashMap(), false));
            return;
        }
        LockContext lockContextExist = CURRENT_LOCK_CONTEXT.get();
        if (lockContextExist == null) {
            Map<String, Set<String>> operateTableMap = Maps.newHashMap();
            operateTableMap.put(tableName, operateSet);
            CURRENT_LOCK_CONTEXT.set(new LockContext(resourceId, operateTableMap, requireLock));
        } else {
            if (!resourceId.equals(lockContextExist.getResourceId())) {
                throw new IllegalStateException("分支注册必须是同一个resourceId!");
            }
            Map<String, Set<String>> operateTableMapExist = lockContextExist.getOperateMap();
            if (operateTableMapExist.containsKey(tableName)) {
                operateTableMapExist.get(tableName).addAll(operateSet);
            } else {
                operateTableMapExist.put(tableName, operateSet);
            }
            lockContextExist.setOperateMap(operateTableMapExist);
            lockContextExist.setRequireLock(requireLock);
            CURRENT_LOCK_CONTEXT.set(lockContextExist);
            refresh(lockContextExist, CURRENT_LOCK_CONTEXT);
        }
    }

    public static void addXAContext(XAContext xaContext) {
        Map<Connection, XAContext> xaContextMap = XA_CONTEXT_INFO.get();

        if (xaContextMap == null) {
            xaContextMap = new HashMap<>();
        }
        xaContextMap.put(xaContext.getConnection(), xaContext);
        XA_CONTEXT_INFO.set(xaContextMap);
    }

    public static List<XAContext> getCurrentXAContextInfo() {
        Map<Connection, XAContext> xaContextMap = XA_CONTEXT_INFO.get();
        if (xaContextMap == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(xaContextMap.values());
        }
    }

    public static void removeXAContext(Connection connection) {
        XA_CONTEXT_INFO.get().remove(connection);
    }


    public static <T, P extends ThreadLocal<T>> void refresh(T t, P p) {
        if (p.get() != null) {
            p.remove();
        }
        p.set(t);
    }
}
