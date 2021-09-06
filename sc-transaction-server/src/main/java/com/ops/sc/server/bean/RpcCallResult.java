package com.ops.sc.server.bean;

import com.ops.sc.rpc.dto.ParentResponse;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Builder
public class RpcCallResult<T> {
    @Getter
    @Setter
    private ParentResponse parentResponse;
    @Getter
    @Setter
    private T response;
    @Setter
    private boolean isSuccess;

    public RpcCallResult(ParentResponse parentResponse, T response, boolean isSuccess) {
        this.parentResponse = parentResponse;
        this.response = response;
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess(){
        return isSuccess;
    }
}