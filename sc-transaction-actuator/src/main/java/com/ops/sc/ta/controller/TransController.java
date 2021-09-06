package com.ops.sc.ta.controller;

import com.ops.sc.common.bean.ScRequestMessage;
import com.ops.sc.common.bean.ScResponseMessage;
import com.ops.sc.core.rest.annotation.*;
import com.ops.sc.ta.handler.TransactionHandlerFactory;
import com.ops.sc.core.rest.Http;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ScController
@RootContext("/ta/api")
public class TransController{


    @MapPath(method = Http.POST, path = "/deal")
    public ScResponseMessage prepare(@RequestBody final ScRequestMessage scRequestMessage) {
        return TransactionHandlerFactory.getInstance().handler(scRequestMessage);
    }

    @MapPath(method = Http.POST, path = "/query")
    public ScResponseMessage commit(@RequestBody final ScRequestMessage scRequestMessage) {
        return TransactionHandlerFactory.getInstance().handler(scRequestMessage);
    }

}
