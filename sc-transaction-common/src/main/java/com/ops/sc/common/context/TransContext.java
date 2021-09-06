package com.ops.sc.common.context;


import com.ops.sc.common.enums.TransIsolation;
import com.ops.sc.common.enums.TransStatus;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@ToString
public class TransContext implements Serializable {

    private String businessId;
    private Long tid;
    private String branchId;
    private TransStatus transStatus;
    private Boolean isTransaction;
    private Map<String, String> attachments = new ConcurrentHashMap<String, String>();
    private Long parentId;
    // 当前线程事务id
    private Long currentTransactionId;
    // 服务端地址
    private String serverAddress;

    public TransContext(String businessId, String serverAddress) {
        this.businessId = businessId;
        this.serverAddress = serverAddress;
    }

    // 传递parentId,tid和serverAddress
    public TransContext(String businessId, Long parentId, String serverAddress, Boolean isTransaction) {
        this.businessId = businessId;
        this.parentId = parentId;
        this.serverAddress = serverAddress;
        this.isTransaction = isTransaction;
    }

    public TransContext(boolean isTransaction) {
        this.isTransaction = isTransaction;
    }

    public TransContext(String businessId, TransStatus transStatus) {
        this.businessId = businessId;
        this.transStatus = transStatus;
    }

    /**
     * tcc模式下， 注册分支之后，将branchId作为当前线程currentTransactionId
     *
     * @param businessId
     * @param transStatus
     * @param currentTransactionId
     */
    public TransContext(String businessId, TransStatus transStatus, Long currentTransactionId) {
        this.businessId = businessId;
        this.transStatus = transStatus;
        this.currentTransactionId = currentTransactionId;
    }

    /**
     * fmt模式下， transactionId/branchId后，将tid/branchId作为currentTransactionId和parentId
     *
     * @param businessId
     * @param isTransaction
     * @param currentTransactionId
     * @param parentId
     */
    public TransContext(String businessId, Boolean isTransaction, Long currentTransactionId, Long parentId) {
        this.businessId = businessId;
        this.isTransaction = isTransaction;
        this.currentTransactionId = currentTransactionId;
        this.parentId = parentId;
    }


}
