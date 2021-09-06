package com.ops.sc.server.service;

import com.ops.sc.common.model.TransactionInfo;


public interface TransExecuteService {

    void submitGlobalTrans(TransactionInfo transactionInfo);
}
