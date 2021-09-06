package com.ops.sc.core.heartbeat;


import com.ops.sc.common.constant.RpcConstants;
import com.ops.sc.common.heartbeat.HeartBeatStatusPublisher;
import com.ops.sc.common.heartbeat.HeartBeatStatusSubscriber;
import com.ops.sc.core.rpc.RpcCallBackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Observable;


@Component
public class HeartBeatStatusSubscriberImpl implements HeartBeatStatusSubscriber {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConstants.SC_LOG);

    @Override
    public void update(Observable observable, Object arg) {
        HeartBeatStatusPublisher publisher = (HeartBeatStatusPublisher) arg;
        String appNameUnique = publisher.getKey();
        LOGGER.info("Remove local observer by key: {}", appNameUnique);
        RpcCallBackService.removeLocalObserverByAppName(appNameUnique);
    }
}
