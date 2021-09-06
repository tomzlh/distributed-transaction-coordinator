package com.ops.sc.tc.service.impl;


import com.ops.sc.common.bean.ModelDetail;
import com.ops.sc.common.bean.TransactionModel;
import com.ops.sc.common.model.ModelDetailDo;
import com.ops.sc.common.model.TransactionModelDo;
import com.ops.sc.common.thread.NamedThreadFactory;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.mybatis.mapper.ModelDetailServiceMapper;
import com.ops.sc.mybatis.mapper.ModelServiceMapper;
import com.ops.sc.tc.service.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class ModelServiceImpl implements ModelService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceImpl.class);

    @Resource
    private ModelDetailServiceMapper modelDetailServiceMapper;

    @Resource
    private ModelServiceMapper modelServiceMapper;

    private Map<String, TransactionModel> modelMap=new ConcurrentHashMap<>();

    protected final ScheduledExecutorService timerExecutor = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("timer-model", 1, true));

    @PostConstruct
    public void init(){
        timerExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("start to load the models!");
                try {
                    List<TransactionModelDo> transactionModelList = modelServiceMapper.loadAllTransModel();
                    if (transactionModelList != null && !transactionModelList.isEmpty()) {
                        for (TransactionModelDo transactionModelDo : transactionModelList) {
                            List<ModelDetailDo> modelDetails = modelDetailServiceMapper.findModelByModelId(transactionModelDo.getId());
                            TransactionModel transactionModel = transactionModelDoToTransactionModel(transactionModelDo);
                            if (!CollectionUtils.isEmpty(modelDetails)) {
                                List<ModelDetail> modelDetailList = new ArrayList<>();
                                for (ModelDetailDo modelDetailDo : modelDetails) {
                                    modelDetailList.add(modelDetailDoToModelDetailDo(modelDetailDo));
                                }
                                transactionModel.setModelDetailList(modelDetailList);
                                modelMap.put(transactionModelDo.getTransCode(), transactionModel);
                            }
                        }
                    }
                }catch (Exception e){
                    LOGGER.error("query trans model error!",e);
                }
                LOGGER.info("finished to load the models!");
            }
        },0,120 * 1000L, TimeUnit.MILLISECONDS);
    }

    private TransactionModel transactionModelDoToTransactionModel(TransactionModelDo transactionModelDo){
        TransactionModel transactionModel=new TransactionModel();
        transactionModel.setId(transactionModelDo.getId());
        transactionModel.setCallMode(transactionModelDo.getCallMode());
        transactionModel.setDesc(transactionModelDo.getRemark());
        transactionModel.setModelName(transactionModelDo.getModelName());
        transactionModel.setTransactionName(transactionModelDo.getTransactionName());
        transactionModel.setTimeout(transactionModelDo.getTimeout());
        transactionModel.setTimeoutType(transactionModelDo.getTimeoutType());
        transactionModel.setTransCode(transactionModelDo.getTransCode());
        transactionModel.setTransGroupId(transactionModelDo.getTransGroupId());
        transactionModel.setTransMode(transactionModelDo.getTransMode());
        return transactionModel;
    }

    private ModelDetail modelDetailDoToModelDetailDo(ModelDetailDo modelDetailDo){
        ModelDetail modelDetail=new ModelDetail();
        modelDetail.setModelBranchName(modelDetailDo.getModelBranchName());
        modelDetail.setBranchName(modelDetailDo.getBranchName());
        modelDetail.setHasParent(modelDetailDo.getHasParent());
        modelDetail.setId(modelDetailDo.getId());
        modelDetail.setModelId(modelDetailDo.getModelId());
        modelDetail.setRetryCount(modelDetailDo.getRetryCount());
        modelDetail.setRetryRequired(modelDetailDo.getRetryRequired());
        if(modelDetailDo.getRequestParams()!=null){
            modelDetail.setRequestParamNames(JsonUtil.toObject(List.class,modelDetailDo.getRequestParams()));
        }
        if(modelDetailDo.getExternalParams()!=null){
            modelDetail.setExternalParamNames(JsonUtil.toObject(List.class,modelDetailDo.getExternalParams()));
        }
        if(modelDetailDo.getParentNames()!=null){
            modelDetail.setParentNames(JsonUtil.toObject(List.class,modelDetailDo.getParentNames()));
        }
        if(modelDetailDo.getResParams()!=null){
            modelDetail.setResParamsList(JsonUtil.toObject(List.class,modelDetailDo.getResParams()));
        }
        return modelDetail;
    }

    @Override
    public TransactionModel getModel(String transCode) {
        return modelMap.get(transCode);
    }
}
