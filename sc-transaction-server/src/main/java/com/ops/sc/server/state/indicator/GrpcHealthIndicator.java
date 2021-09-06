package com.ops.sc.server.state.indicator;


import com.google.common.collect.Maps;
import com.ops.sc.rpc.health.HealthCheck;
import com.ops.sc.rpc.health.HealthGrpc;
import com.ops.sc.server.state.HealthCheckServiceImpl;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class GrpcHealthIndicator extends AbstractHealthIndicator {

    private static final String LOCALHOST = "127.0.0.1";
    private Map<String, ManagedChannel> channelFactory = Maps.newHashMap();

    public void init(int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(LOCALHOST, port).usePlaintext().build();
        channelFactory.put(LOCALHOST, channel);
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        doGrpcHealthCheck(builder);
    }

    private void doGrpcHealthCheck(Health.Builder builder) {
        Channel channel = channelFactory.get(LOCALHOST);
        HealthGrpc.HealthBlockingStub healthBlockingStub = HealthGrpc.newBlockingStub(channel);
        HealthCheck.HealthCheckRequest request = HealthCheck.HealthCheckRequest.newBuilder()
                .setService(HealthCheckServiceImpl.DELEGATE_SERVICE).build();
        try {
            HealthCheck.HealthCheckResponse response = healthBlockingStub.check(request);
            if (HealthCheck.HealthCheckResponse.ServingStatus.SERVING == response.getStatus()) {
                builder.up();
            } else {
                builder.down();
            }
        } catch (Exception e) {
            builder.down(e);
        }
    }
}
