package com.ops.sc.common.bean;

import com.ops.sc.common.model.TransMQProducer;


public class TransMQProducerQueryParams extends TransMQProducer {

    private Page page; // 分页信息

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

}
