package com.ops.sc.tc.controller;

import com.ops.sc.common.bean.ResultCode;
import com.ops.sc.common.bean.TransactionModel;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.core.rest.Http;
import com.ops.sc.core.rest.annotation.MapPath;
import com.ops.sc.core.rest.annotation.RequestBody;
import com.ops.sc.core.rest.annotation.RootContext;
import com.ops.sc.core.rest.annotation.ScController;
import com.ops.sc.core.util.ApplicationUtils;
import com.ops.sc.rpc.dto.GlobalTransRollbackRequest;
import com.ops.sc.tc.api.TransOperationApi;
import com.ops.sc.tc.build.GlobalTransBeanBuilder;
import com.ops.sc.tc.model.*;
import com.ops.sc.tc.service.ModelService;
import com.ops.sc.tc.service.impl.ModelServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

@Slf4j
@ScController
@RootContext("/ts/api")
public class SponsorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SponsorController.class);


    private final static Long DEFAULT_TIMEOUT=3000L;

    @MapPath(method = Http.POST, path = "/transaction")
    public ApiResponse transaction(@RequestBody final GlobalTransRequest request){
         try {
             checkParams(request);
             TransOperationApi transOperationApi = ApplicationUtils.getBean(TransOperationApi.class);
             com.ops.sc.rpc.dto.GlobalTransRequest globalTransRequest = GlobalTransBeanBuilder.buildGlobalTransRequest(request);
             transOperationApi.startGlobalTrans(globalTransRequest, request.getTimeout());
         }catch (ScClientException se){
             LOGGER.error("start global transaction error:{}",request,se);
             ApiResponse.builder().businessId(request.getBusinessId()).code(se.getClientErrorCode().getErrorCode()).message(se.getMessage()).build();
         }
         catch (Exception e){
             LOGGER.error("start global transaction error:{}",request,e);
             ApiResponse.builder().businessId(request.getBusinessId()).code(ClientErrorCode.INTERNAL_ERROR.getErrorCode()).message(e.getMessage()).build();
         }
         return ApiResponse.builder().businessId(request.getBusinessId()).code(ClientErrorCode.SUCCESS.getErrorCode()).message(null).build();
    }

    private void checkParams(GlobalTransRequest request) throws ScClientException {
        if (StringUtils.isBlank(request.getAppName())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "appName is empty!");
        }
        if (StringUtils.isBlank(request.getTransGroupId())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "transGroupId is empty!");
        }
        if (StringUtils.isBlank(request.getBusinessId())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "businessId is empty!");
        }
        if (StringUtils.isBlank(request.getTransCode())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "transCode is empty!");
        }
        if (StringUtils.isBlank(request.getTransMode())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "transMode is empty!");
        }
        if (CollectionUtils.isEmpty(request.getBranchTransRequests())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "branch list is empty!");
        }
    }


    @MapPath(method = Http.POST, path = "/sagatrans")
    public ApiResponse sagatrans(@RequestBody final GlobalSagaTransRequest request){
        try {
            checkParams(request);
            TransOperationApi transOperationApi = ApplicationUtils.getBean(TransOperationApi.class);
            ModelService modelService=ApplicationUtils.getBean(ModelServiceImpl.class);
            TransactionModel transactionModel = modelService.getModel(request.getTransCode());
            com.ops.sc.rpc.dto.GlobalSagaTransRequest globalSagaTransRequest = GlobalTransBeanBuilder.buildGlobalSagaTransRequest(request, transactionModel);
            transOperationApi.startGlobalSagaTrans(request.getTransCode(), globalSagaTransRequest, transactionModel.getTimeout());
        }catch (ScClientException se){
            LOGGER.error("start global saga transaction error:{}",request,se);
            ApiResponse.builder().businessId(request.getBusinessId()).code(se.getClientErrorCode().getErrorCode()).message(se.getMessage()).build();
        }
        catch (Exception e){
            LOGGER.error("start global saga transaction error:{}",request,e);
            ApiResponse.builder().businessId(request.getBusinessId()).code(ClientErrorCode.INTERNAL_ERROR.getErrorCode()).message(e.getMessage()).build();
        }
        return ApiResponse.builder().businessId(request.getBusinessId()).code(ClientErrorCode.SUCCESS.getErrorCode()).message(null).build();
    }

    private void checkParams(GlobalSagaTransRequest request) throws ScClientException {
        if (StringUtils.isBlank(request.getAppName())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "appName is empty!");
        }
        if (StringUtils.isBlank(request.getBusinessId())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "businessId is empty!");
        }
        if (StringUtils.isBlank(request.getTransCode())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "transCode is empty!");
        }
        if (CollectionUtils.isEmpty(request.getBranchTransRequests())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "branch list is empty!");
        }
    }

    @MapPath(method = Http.POST, path = "/rollback")
    public ApiResponse rollback(@RequestBody final GlobalRollbackRequest globalRollbackRequest){
        try {
            checkParams(globalRollbackRequest);
            TransOperationApi transOperationApi = ApplicationUtils.getBean(TransOperationApi.class);
            GlobalTransRollbackRequest globalTransRollbackRequest=GlobalTransBeanBuilder.buildGlobalRollbackRequest(globalRollbackRequest);
           transOperationApi.rollbackGlobalTrans(globalTransRollbackRequest, globalRollbackRequest.getTimeout());
        }catch (ScClientException se){
            LOGGER.error("rollback global transaction error:{}", globalRollbackRequest,se);
            ApiResponse.builder().businessId(globalRollbackRequest.getBusinessId()).code(se.getClientErrorCode().getErrorCode()).message(se.getMessage()).build();
        }
        catch (Exception e){
            LOGGER.error("rollback global transaction error:{}", globalRollbackRequest,e);
            ApiResponse.builder().businessId(globalRollbackRequest.getBusinessId()).code(ClientErrorCode.INTERNAL_ERROR.getErrorCode()).message(e.getMessage()).build();
        }
        return ApiResponse.builder().businessId(globalRollbackRequest.getBusinessId()).code(ClientErrorCode.SUCCESS.getErrorCode()).build();
    }

    private void checkParams(GlobalRollbackRequest request) throws ScClientException {
        if (StringUtils.isBlank(request.getTid())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "tid is empty!");
        }
        if (StringUtils.isBlank(request.getBusinessId())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "businessId is empty!");
        }
        if (StringUtils.isBlank(request.getTransMode())) {
            throw new ScClientException(ClientErrorCode.PARAM_MISSING_ERROR, "transMode is empty!");
        }
    }

    @MapPath(method = Http.POST, path = "/querytrans")
    public TransQueryResponse querytrans(@RequestBody final TransQueryRequest transQueryRequest){
        TransQueryResponse response = null;
         try {
             TransOperationApi transOperationApi = ApplicationUtils.getBean(TransOperationApi.class);
             com.ops.sc.rpc.dto.TransQueryRequest rpcTransQueryRequest = com.ops.sc.rpc.dto.TransQueryRequest.newBuilder().setBusinessId(transQueryRequest.getBusinessId()).build();
             com.ops.sc.rpc.dto.TransQueryResponse transQueryResponse = transOperationApi.queryGlobalTrans(rpcTransQueryRequest, transQueryRequest.getTimeout() == null ? DEFAULT_TIMEOUT : transQueryRequest.getTimeout());
             response=GlobalTransBeanBuilder.buildTransQueryRequest(transQueryResponse);
             return response;
         }catch (Exception e){
             LOGGER.error("query global transaction error:{}", transQueryRequest,e);
             response=new TransQueryResponse();
             response.setCode(ResultCode.Failed.name());
             response.setMessage(e.getMessage());
         }
         return response;
    }
}
