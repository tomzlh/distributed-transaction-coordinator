package com.ops.sc.ta.trans.xa;

import com.ops.sc.common.bean.*;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.ta.executor.XaClientExecutor;
import com.ops.sc.common.bean.XATid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;

@Service
public class XATaExecutorImpl implements XaClientExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(XATaExecutorImpl.class);

    @Override
    public ScResponseMessage prepare(ScRequestMessage branchPrepareRequest) throws ScClientException{
        ScResponseMessage branchPrepareResponse = null;
        try {
            XADataSource xaDataSource = XADataSourceRecorder
                    .getXADataSourceByBeanName(branchPrepareRequest.getDataSource());
            if (xaDataSource == null) {
                throw new NoSuchBeanDefinitionException(branchPrepareRequest.getDataSource());
            }
            XATid xaTid = new XATid(branchPrepareRequest.getTid(), branchPrepareRequest.getBranchId());
            XATransactionManager.getInstance().prepareXA(xaTid,xaDataSource.getTargetXADataSource());
            LOGGER.debug("Prepare xa success, tid: {}, branchId: {}", xaTid.getTid(), xaTid.getBranchId());
        }catch (Exception e){
            LOGGER.error("Prepare xa failed, branchPrepareRequest: {}", branchPrepareRequest);
            ScResponseMessage.ResultInfo resultInfo=new ScResponseMessage.ResultInfo();
            resultInfo.code= TransactionResponseCode.BRANCH_PREPARE_FAILED.getCode();
            resultInfo.message="Prepare xa failed!"+e.getMessage();
            branchPrepareResponse = getBranchPrepareResponse(branchPrepareRequest.getBusinessId(),branchPrepareRequest.getTid(),branchPrepareRequest.getBranchId(),resultInfo);
            return branchPrepareResponse;
        }
        ScResponseMessage.ResultInfo resultInfo=new ScResponseMessage.ResultInfo();
        resultInfo.code= TransactionResponseCode.SUCCESS.getCode();
        resultInfo.message="Prepare xa succeed!";
        branchPrepareResponse = getBranchPrepareResponse(branchPrepareRequest.getBusinessId(),branchPrepareRequest.getTid(),branchPrepareRequest.getBranchId(),resultInfo);
        return branchPrepareResponse;
    }

    private ScResponseMessage getBranchPrepareResponse(String businessId, long tid, long bid, ScResponseMessage.ResultInfo resultInfo){
        BranchPrepareResponse branchPrepareResponse=new BranchPrepareResponse();
        branchPrepareResponse.setBranchId(bid);
        branchPrepareResponse.setBusinessId(businessId);
        branchPrepareResponse.setTid(tid);
        branchPrepareResponse.setResultInfo(resultInfo);
        //branchPrepareResponse.setMsg(msg);
        return branchPrepareResponse;
    }

    @Override
    public ScResponseMessage confirm(ScRequestMessage branchCommitRequest) throws ScClientException {
        ScResponseMessage branchCommitResponse=null;
        try {
            XADataSource xaDataSource = XADataSourceRecorder
                    .getXADataSourceByBeanName(branchCommitRequest.getDataSource());
            if (xaDataSource == null) {
                throw new NoSuchBeanDefinitionException(branchCommitRequest.getDataSource());
            }
            XATid xaTid = new XATid(branchCommitRequest.getTid(), branchCommitRequest.getBranchId());
            XATransactionManager.getInstance().commitXA(xaTid, xaDataSource.getTargetXADataSource());
            LOGGER.debug("Commit xa success, tid: {}, branchId: {}", xaTid.getTid(), xaTid.getBranchId());
        }catch (Exception e){
            LOGGER.error("Commit xa failed, tid: {}, branchId: {}", branchCommitRequest.getTid(), branchCommitRequest.getBranchId());
            ScResponseMessage.ResultInfo resultInfo=new ScResponseMessage.ResultInfo();
            resultInfo.code= TransactionResponseCode.BRANCH_COMMIT_FAILED.getCode();
            resultInfo.message="Commit xa failed!"+e.getMessage();
            branchCommitResponse = getBranchCommitResponse(branchCommitRequest.getBusinessId(),branchCommitRequest.getTid(),branchCommitRequest.getBranchId(),resultInfo);
            return branchCommitResponse;
        }
        ScResponseMessage.ResultInfo resultInfo=new ScResponseMessage.ResultInfo();
        resultInfo.code= TransactionResponseCode.SUCCESS.getCode();
        resultInfo.message="Commit xa succeed!";
        branchCommitResponse = getBranchCommitResponse(branchCommitRequest.getBusinessId(),branchCommitRequest.getTid(),branchCommitRequest.getBranchId(),resultInfo);
        return branchCommitResponse;
    }

    private ScResponseMessage getBranchCommitResponse(String businessId, long tid, long bid, ScResponseMessage.ResultInfo resultInfo){
        ScResponseMessage branchCommitResponse=new ScResponseMessage();
        branchCommitResponse.setBranchId(bid);
        branchCommitResponse.setBusinessId(businessId);
        branchCommitResponse.setTid(tid);
        branchCommitResponse.setResultInfo(resultInfo);
        //branchCommitResponse.setMsg(msg);
        return branchCommitResponse;
    }

    @Override
    public ScResponseMessage cancel(ScRequestMessage branchRollbackRequest) throws ScClientException{
        ScResponseMessage branchRollbackResponse=null;
        try {
            XADataSource xaDataSource = XADataSourceRecorder
                    .getXADataSourceByBeanName(branchRollbackRequest.getDataSource());
            if (xaDataSource == null) {
                throw new NoSuchBeanDefinitionException(branchRollbackRequest.getDataSource());
            }
            XATid xaTid = new XATid(branchRollbackRequest.getTid(), branchRollbackRequest.getBranchId());
            XATransactionManager.getInstance().rollbackXA(xaTid, xaDataSource.getTargetXADataSource());
            LOGGER.debug("Rollback xa success, tid: {}, branchId: {}", xaTid.getTid(), xaTid.getBranchId());
        }catch (Exception e){
            LOGGER.debug("Rollback xa failed, tid: {}, branchId: {}", branchRollbackRequest.getTid(), branchRollbackRequest.getBranchId());
            ScResponseMessage.ResultInfo resultInfo=new ScResponseMessage.ResultInfo();
            resultInfo.code= TransactionResponseCode.BRANCH_ROLLBACK_FAILED.getCode();
            resultInfo.message="Rollback xa failed!"+e.getMessage();
            branchRollbackResponse = getBranchRollbackResponse(branchRollbackRequest.getBusinessId(),branchRollbackRequest.getTid(),branchRollbackRequest.getBranchId(),resultInfo);
            return branchRollbackResponse;
        }
        ScResponseMessage.ResultInfo resultInfo=new ScResponseMessage.ResultInfo();
        resultInfo.code= TransactionResponseCode.SUCCESS.getCode();
        resultInfo.message="Rollback xa succeed!";
        branchRollbackResponse = getBranchRollbackResponse(branchRollbackRequest.getBusinessId(),branchRollbackRequest.getTid(),branchRollbackRequest.getBranchId(),resultInfo);
        return branchRollbackResponse;
    }

    private ScResponseMessage getBranchRollbackResponse(String businessId, long tid, long bid, ScResponseMessage.ResultInfo resultInfo){
        BranchRollbackResponse branchRollbackResponse=new BranchRollbackResponse();
        branchRollbackResponse.setBranchId(bid);
        branchRollbackResponse.setBusinessId(businessId);
        branchRollbackResponse.setTid(tid);
        branchRollbackResponse.setResultInfo(resultInfo);
        //branchRollbackResponse.setMsg(msg);
        return branchRollbackResponse;
    }


    @Override
    public String transactionName() {
        return "XA_EXECUTOR_DEFAULT";
    }
}
