package com.ops.sc.core.build;

import com.ops.sc.common.bean.ScRequestMessage;
import com.ops.sc.core.model.RpcTransMessage;

import java.util.concurrent.atomic.AtomicInteger;

public class RpcMessageBuilder {

    private static AtomicInteger atom = new AtomicInteger(0);

    private static final int MASK = 0x7FFFFFFF;



    public static RpcTransMessage toRpcTransMessage(RpcTransMessage event, ScRequestMessage scRequestMessage) {
        event.setId(scRequestMessage.getTid());
        event.setScRequestMessage(scRequestMessage);
        event.setMessageType(scRequestMessage.getMessageType());
        return  event;
    }



    public final static int incrementAndGet() {
        return atom.incrementAndGet() & MASK;
    }
}
