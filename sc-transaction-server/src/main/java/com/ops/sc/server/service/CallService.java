package com.ops.sc.server.service;

import com.ops.sc.common.bean.ScRequestMessage;
import com.ops.sc.common.bean.ScResponseMessage;
import com.ops.sc.common.bean.ScSagaRequestMessage;
import com.ops.sc.common.exception.ScServerException;

import java.util.Map;

public interface CallService {

     ScResponseMessage call(String url, ScRequestMessage scRequestMessage) throws ScServerException;

     Map<String,String> call(String url,  Map<String,Object> map) throws ScServerException;

}
