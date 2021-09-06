package com.ops.sc.admin.service.impl;

import com.ops.sc.admin.conf.AppConfiguration;
import com.ops.sc.admin.dao.TransBranchInfoDao;
import com.ops.sc.admin.dao.TransInfoDao;
import com.ops.sc.admin.grpc.GrpcInitParams;
import com.ops.sc.admin.grpc.sync.ScAdminGrpcSyncClient;
import com.ops.sc.admin.grpc.sync.ScAdminGrpcSyncClientBoot;
import com.ops.sc.common.bean.ResultCode;
import com.ops.sc.common.bean.TransInfoQueryParams;
import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.constant.RpcConstants;
import com.ops.sc.common.exception.ScServerException;
import com.ops.sc.common.dto.admin.GrpcStreamMapResult;
import com.google.common.collect.Lists;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.enums.TransStatus;
import com.ops.sc.common.reg.zk.ZookeeperRegistryCenter;
import com.ops.sc.common.store.ScBranchRecord;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.core.build.RpcRequestBuilder;
import com.ops.sc.core.rpc.RpcCallBackService;
import com.ops.sc.admin.service.OpsService;
import com.ops.sc.admin.service.TransGroupService;
import com.ops.sc.rpc.dto.BranchTransRequest;
import com.ops.sc.rpc.dto.BranchTransResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class OpsServiceImpl implements OpsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpsServiceImpl.class);

    @Resource
    private TransGroupService transGroupService;

    @Resource
    private TransBranchInfoDao transBranchInfoDao;

    @Resource
    private AppConfiguration appConfiguration;

    @Resource
    private TransInfoDao transInfoDao;

    public final static Long RPC_REQUEST_TIMEOUT=3000L;

    private final String SERVER_PATHS="/sc/registry/grpc/";



    @Override
    public ResponseResult retryFailBranchTrans(String tenantId, Long tid, Long branchId) {
        LOGGER.info("Start to retryFailBranch. tid : {}, branchId : {}", tid, branchId);
        ScBranchRecord scBranchRecord = getUserBranchTrans(tenantId, tid, branchId);

        if (scBranchRecord == null) {
            throw new ScServerException(TransactionResponseCode.BRANCH_TRANS_NOT_EXIST,"branch transaction is not exist!");
        }
        TransStatus branchStatus = TransStatus.getTransStatusByValue(scBranchRecord.getStatus());
        if (branchStatus != TransStatus.CANCEL_FAILED && branchStatus != TransStatus.COMMIT_FAILED) {
            throw new ScServerException(TransactionResponseCode.BRANCH_TRANS_OPERATE_ILLEGAL,"branch transaction status is not ok!");
        }
        ScTransRecord transactionInfo = transInfoDao.findByTid(tid);
        if (transactionInfo == null) {
            throw new ScServerException(TransactionResponseCode.TRANS_NOT_EXIST,"Global transaction is not exist!");
        }
        scBranchRecord.setStatus(TransStatus.TRY_SUCCEED.getValue());
        ZookeeperRegistryCenter.getInstance().init();
        List<String> list= ZookeeperRegistryCenter.getInstance().getChildren(SERVER_PATHS+appConfiguration.getServerCluster());
        ScAdminGrpcSyncClient scAdminGrpcSyncClient=null;
        ScAdminGrpcSyncClientBoot compensatorGrpcSyncClientBoot=null;
        String serverAddress=null;
        if(list!=null&&!list.isEmpty()) {
            serverAddress=list.get(ThreadLocalRandom.current().nextInt(list.size()));
            GrpcInitParams sponsorGrpcSyncClientParams=new GrpcInitParams(appConfiguration.getAppName(), RpcConstants.TA_MAX_CHANNEL_COUNT,RpcConstants.SHUTDOWN_TIMEOUT_MILLS,RpcConstants.DEFAULT_TSCLIENT_THREAD_POOL_SIZE);
            compensatorGrpcSyncClientBoot=ScAdminGrpcSyncClientBoot.getInstance();
            compensatorGrpcSyncClientBoot.initClientConfig(sponsorGrpcSyncClientParams);
            scAdminGrpcSyncClient=compensatorGrpcSyncClientBoot.getClient(serverAddress);
        }
        boolean isSucceed=true;
        try {
            BranchTransResponse branchTransResponse=null;
            if (branchStatus == TransStatus.CANCEL_FAILED) {
                BranchTransRequest branchTransRequest = RpcRequestBuilder.buildBranchTransRequest(scBranchRecord.getBusinessId(), Constants.CANCEL, scBranchRecord);
                branchTransResponse=scAdminGrpcSyncClient.rollbackBranchTransSync(branchTransRequest, RPC_REQUEST_TIMEOUT);
            } else {
                BranchTransRequest branchTransRequest = RpcRequestBuilder.buildBranchTransRequest(scBranchRecord.getBusinessId(), Constants.COMMIT, scBranchRecord);
                branchTransResponse = scAdminGrpcSyncClient.commitBranchTransSync(branchTransRequest, RPC_REQUEST_TIMEOUT);
            }
            if(branchTransResponse==null||!branchTransResponse.getBaseResponse().getCode().equals(ResultCode.Success)){
                isSucceed=false;
            }
        }catch (Exception e){
            LOGGER.error("retry branch transaction error:{}",scBranchRecord,e);
            isSucceed=false;
        }finally {
            try {
                if(compensatorGrpcSyncClientBoot!=null){
                    compensatorGrpcSyncClientBoot.getInstance().closeClient(serverAddress);
                }
            }catch (Exception e){
                LOGGER.error("close the grpc client error!",e);
            }
            try{
                ZookeeperRegistryCenter.getInstance().close();
            }catch (Exception e){
                LOGGER.error("close zk connection error!",e);
            }
        }
        if(isSucceed){
            transBranchInfoDao.updateStatusByBranchId(scBranchRecord.getTid(), branchStatus==TransStatus.COMMIT_FAILED?TransStatus.COMMIT_SUCCEED.getValue() :TransStatus.CANCEL_SUCCEED.getValue(), scBranchRecord.getRetryCount()+1);
        }
        else {
            transBranchInfoDao.updateStatusByBranchId(scBranchRecord.getTid(), branchStatus==TransStatus.COMMIT_FAILED?TransStatus.COMMIT_FAILED.getValue() :TransStatus.CANCEL_FAILED.getValue(), scBranchRecord.getRetryCount()+1);
            throw new ScServerException(TransactionResponseCode.BRANCH_TRANS_OPERATE_ILLEGAL,"branch trans operate failed!");
        }
        return ResponseResult.returnSuccess();
    }

    @Override
    public ResponseResult cancelTimeoutBranchTrans(String tenantId, Long tid, Long branchId) {
        ScBranchRecord scBranchRecord = getUserBranchTrans(tenantId, tid, branchId);
        if (scBranchRecord == null) {
            throw new ScServerException(TransactionResponseCode.BRANCH_TRANS_NOT_EXIST,"Branch transaction is not exist!");
        }
        if (!scBranchRecord.getStatus().equals(TransStatus.TRY_TIMEOUT.getValue())) {
            throw new ScServerException(TransactionResponseCode.BRANCH_TRANS_OPERATE_ILLEGAL,"branch transaction status is not ok!");
        }

        LOGGER.info("Start to cancel timeout branch transaction, tid : {}, branchId : {}, cancelAtOnce : {}",
                scBranchRecord.getTid(), scBranchRecord.getBid());

        ScTransRecord scTransRecord = transInfoDao.findByTid(tid);
        if (scTransRecord == null) {
            throw new ScServerException(TransactionResponseCode.TRANS_NOT_EXIST,"Transaction is not exist!");
        }

        if (transBranchInfoDao.updateStatusByBranchIdAndStatus(scBranchRecord.getTid(), scBranchRecord.getBid(),
                Collections.singletonList(scBranchRecord.getStatus()), TransStatus.TRY_FAILED.getValue()) == 0) {
            throw new ScServerException(TransactionResponseCode.BRANCH_TRANS_OPERATE_ILLEGAL,"branch transaction operate failed!");
        }
        return ResponseResult.returnSuccess();
    }

    @Override
    public ResponseResult checkBackTimeoutGlobalTrans(String tenantId, Long tid) {

        Map<String, String> groupId2Name = transGroupService.getGroupId2NameMap(tenantId);
        List<String> groupIdList = new ArrayList<>(groupId2Name.keySet());
        if (CollectionUtils.isEmpty(groupIdList)) {
            throw new ScServerException(TransactionResponseCode.TRANS_NOT_EXIST,"Transaction is not exist!");
        }

        ScTransRecord scTransRecord = transInfoDao.findByTid(tid);
        if (scTransRecord == null || !groupIdList.contains(scTransRecord.getGroupId())) {
            throw new ScServerException(TransactionResponseCode.TRANS_NOT_EXIST,"Transaction is not exist!");
        }
        if (!TransStatus.TRY_TIMEOUT.getValue().equals(scTransRecord.getStatus())) {
            throw new ScServerException(TransactionResponseCode.GLOBAL_TRANS_OPERATE_ILLEGAL,"Global transaction operate illegal!");
        }

        LOGGER.info("tcc global timeoutMills tid : {} start to checkBack.", scTransRecord.getTid());
        transInfoDao.updateStatusByTidAndStatus(scTransRecord.getTid(), scTransRecord.getStatus(), TransStatus.TRYING.getValue(),0);
        return ResponseResult.returnSuccess();
    }


    private ScBranchRecord getUserBranchTrans(String tenantId,Long tid, Long branchId) {
        Map<String, String> groupId2Name = transGroupService.getGroupId2NameMap(tenantId);
        List<String> groupIdList = new ArrayList<>(groupId2Name.keySet());
        if (CollectionUtils.isEmpty(groupIdList)) {
            return null;
        }
        TransInfoQueryParams queryParams = new TransInfoQueryParams();
        queryParams.setGroupIdList(groupIdList);
        queryParams.setTid(tid);
        if (transInfoDao.getTotalCountByConditions(queryParams) == 0) {
            return null;
        }
        return transBranchInfoDao.findByTidAndBid(tid, branchId);
    }

    /**
     * 获取GRPC的Session信息
     *
     * @return
     */
    @Override
    public ResponseResult getGrpcStreamMap() {
        Map<String, List<String>> localStreamList = new HashMap<>();
        Map<String, ConcurrentHashMap<String, StreamObserver>> streamObserverMap = RpcCallBackService.getStreamMap();
        for (Map.Entry<String, ConcurrentHashMap<String, StreamObserver>> iterator : streamObserverMap.entrySet()) {
            localStreamList.put(iterator.getKey(), Lists.newArrayList());
            for (Map.Entry<String, StreamObserver> map : iterator.getValue().entrySet()) {
                localStreamList.get(iterator.getKey()).add(map.getKey());
            }
        }
        return new GrpcStreamMapResult(localStreamList);
    }

}
