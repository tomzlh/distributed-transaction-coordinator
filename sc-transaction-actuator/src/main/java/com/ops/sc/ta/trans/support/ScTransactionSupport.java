package com.ops.sc.ta.trans.support;

import com.google.protobuf.UInt32Value;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.exception.LockException;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.core.build.LockContextBuilder;
import com.ops.sc.core.clone.context.ImageContextRecorder;
import com.ops.sc.common.context.TransContext;
import com.ops.sc.common.context.TransactionContextRecorder;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.BranchTransResponse;
import com.ops.sc.rpc.dto.GlobalTransRequest;
import com.ops.sc.ta.buid.BranchTransBeanBuilder;
import com.ops.sc.ta.dao.LogDao;
import com.ops.sc.ta.trans.datasource.DatabaseResource;
import com.ops.sc.ta.trans.DefaultTaCallOutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;

import static com.ops.sc.common.constant.Constants.SC_GLOBAL_TRANS_PARAM;
import static com.ops.sc.common.constant.Constants.SC_LOGICAL_BRANCH_REGISTER_PARAM;
import static com.ops.sc.common.enums.TransactionResponseCode.LOCK_CONFLICT;


@Component("transactionSupport")
public class ScTransactionSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScTransactionSupport.class);

    private static ScTransactionSupport instance;

    @Resource(name = "scLogDao")
    private LogDao logDao;

    public static ScTransactionSupport getInstance() {
        assert instance != null;
        return instance;
    }

    @PostConstruct
    public void afterInitialization() {
        instance = this;
    }

    public void beforeTransactionStart() {
        if (ImageContextRecorder.get() == null) {
            ImageContextRecorder.init();
        }
        LOGGER.debug("Init image context, tid : {}", TransactionContextRecorder.getTid());
    }

    public void beforeCommit(DatabaseResource databaseResource) throws ScClientException,SQLException {
        try {
            BranchTransRequest registerBranchRequest = (BranchTransRequest) TransactionContextRecorder
                    .getRegisterContext().get().get(SC_LOGICAL_BRANCH_REGISTER_PARAM);
            if (registerBranchRequest == null) {
                if (TransactionContextRecorder.isInitiatorFromAspect()) {
                    // 发起者本地业务
                    GlobalTransRequest regGlobalTransRequest = (GlobalTransRequest) TransactionContextRecorder
                            .getRegisterContext().get().get(SC_GLOBAL_TRANS_PARAM);
                    if (regGlobalTransRequest == null) {
                        throw new ScClientException(ClientErrorCode.INTERNAL_ERROR,
                                "Branch register params failed!");
                    }
                    registerBranchRequest = BranchTransBeanBuilder
                            .globalParams2FMTBranchParams(regGlobalTransRequest);

                } else if (TransactionContextRecorder.isParticipant()) {
                    throw new ScClientException(ClientErrorCode.INTERNAL_ERROR,
                            "Branch register params failed!");
                } else {
                    throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED,
                            "Branch service not in @DistributeTrans annotation!");
                }
            }
            BranchTransRequest.Builder requestBuilder = registerBranchRequest.toBuilder();
            requestBuilder.setBranchType(UInt32Value.of(TransMode.FMT.getValue()));
            requestBuilder.setLockContext(
                    LockContextBuilder.toLockContextRpcRequest(TransactionContextRecorder.getCurrentLockContext()));
            BranchTransResponse response = DefaultTaCallOutService.getInstance()
                    .registerBranch(requestBuilder.build());
            if (response.getBaseResponse().getCode().equals(LOCK_CONFLICT.getCode())) {
                throw new LockException("Lock conflict tid: " + registerBranchRequest.getTid());
            }
            if (!TransactionResponseCode.getErrorCodeEnum(response.getBaseResponse().getCode()).isSucceed()) {
                throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED,
                        "Begin new branch fail: " + response.getBaseResponse().getMessage());
            }
            String branchId = response.getBranchId();
            LOGGER.info("Begin new fmt branch transaction tid : {}, branchId : {}", requestBuilder.getTid(), branchId);
            TransactionContextRecorder.setCurrentTransContext(
                    new TransContext(requestBuilder.getBusinessId(), TransStatus.TRY_SUCCEED));

        } catch (Exception e) {
            Long tid = TransactionContextRecorder.getTid();
            if (e instanceof LockException) {
                throw new ScClientException(ClientErrorCode.LOCK_CONFLICT, e);
            } else if (e instanceof ScClientException) {
                throw e;
            } else {
                String msg = String.format("Begin new fmt branch transaction fail! tid: %s, branchId: %s", tid,
                        TransactionContextRecorder.getBranchId());
                throw new SQLException(msg, e);
            }
        }
    }

    public void afterTransactionEnd() {
        ImageContextRecorder.remove();
        TransactionContextRecorder.clearCurrentLockContext();
        LOGGER.debug("Remove ImageContext success!");
    }
}
