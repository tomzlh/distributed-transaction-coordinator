package com.ops.sc.server.service.impl;

import com.ops.sc.common.bean.ScRequestMessage;
import com.ops.sc.common.bean.ScResponseMessage;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.exception.ScServerException;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.server.service.CallService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service("httpCallService")
public class HttpCallServiceImpl extends AbstractVerticle implements CallService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCallServiceImpl.class);

    private final String code="02";

    private WebClientOptions webClientOptions;
    private WebClient client;

    @PostConstruct
    public void init(){
        Vertx vertx = Vertx.vertx();
        webClientOptions = new WebClientOptions().setConnectTimeout(1000).setKeepAlive(true).setIdleTimeout(50).setMaxWaitQueueSize(50);
        client = WebClient.create(vertx, webClientOptions);
    }


    @Override
    public ScResponseMessage call(String url, ScRequestMessage scRequestMessage) throws ScServerException {
        CallResult callResult=new CallResult();
        CountDownLatch countDownLatch=new CountDownLatch(1);
        try {
            client.postAbs(url).sendJson(scRequestMessage, requestResponse -> {
                if (requestResponse.succeeded()) {
                    ScResponseMessage responseResult = requestResponse.result().bodyAsJson(ScResponseMessage.class);
                    callResult.responseMessage=responseResult;
                } else {
                    LOGGER.warn("send request error：{} {}", url,scRequestMessage, requestResponse.cause());
                    callResult.isSucceed=false;
                }
                countDownLatch.countDown();
            });
        }catch (Exception e){
            countDownLatch.countDown();
            LOGGER.error("send request error：{} {}", url,scRequestMessage, e);
            throw new ScServerException(TransactionResponseCode.CALL_BRANCH_ERROR,"send request error:"+url,e);
        }
        try {
            countDownLatch.await(2L, TimeUnit.SECONDS);
        }catch (Exception e){}
        if(!callResult.isSucceed){
            throw new ScServerException(TransactionResponseCode.TRANSACTION_PROCESS_FAILED,"branch transaction process failed:"+url);
        }
        return callResult.responseMessage;
    }

    @Override
    public Map<String,String> call(String url,  Map<String,Object> map) throws ScServerException {
        Map<String,String> paramMap=new HashMap<>();
        CallResult callResult=new CallResult();
        CountDownLatch countDownLatch=new CountDownLatch(1);
        try {
            client.postAbs(url).sendJson(JsonUtil.toString(map), requestResponse -> {
                try {
                    String responseResult = requestResponse.result().bodyAsString();
                    if (requestResponse.succeeded()) {
                        if (responseResult != null) {
                            paramMap.putAll(JsonUtil.toMap(responseResult));
                        }
                        if (paramMap.get("status") == null || !"00".equals(paramMap.get("status"))) {
                            LOGGER.warn("send request error：{} {}", url, map);
                            callResult.isSucceed = false;
                        }
                    } else {
                        LOGGER.warn("send request error：{} {} {}", url, map, responseResult);
                        callResult.isSucceed = false;
                    }
                }catch (Exception e){
                    callResult.isSucceed = false;
                    LOGGER.error("call branch error：{} {} response: {}", url, map,requestResponse,e);
                }
                countDownLatch.countDown();
            });
        }catch (Exception e){
            countDownLatch.countDown();
            LOGGER.error("send request error：{} {}", url, map, e);
            throw new ScServerException(TransactionResponseCode.CALL_BRANCH_ERROR,"send request error:"+url,e);
        }
        try {
            countDownLatch.await(2L, TimeUnit.SECONDS);
        }catch (Exception e){}
        if(!callResult.isSucceed){
            throw new ScServerException(TransactionResponseCode.TRANSACTION_PROCESS_FAILED,"branch transaction process failed:"+url);
        }
        return paramMap;
    }

    private int getPortFromUrl(String url){
        int port=-1;
        if (url.contains(":")) {
            port = Integer.parseInt(url.substring(url.lastIndexOf(":")+1,url.length()));
        }
        return port;
    }

    private String getIpFromUrl(String url){
        String ip = url;
        if (url.contains(":")) {
            ip = url.substring(0, url.lastIndexOf(":"));
        }
        return ip;
    }

    private class CallResult{
       public boolean isSucceed=true;
       public ScResponseMessage responseMessage;
    }

    public void close(){
        client.close();
    }

}
