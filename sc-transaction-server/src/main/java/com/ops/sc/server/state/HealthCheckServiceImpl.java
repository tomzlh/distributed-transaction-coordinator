package com.ops.sc.server.state;

import com.google.common.collect.Maps;

import com.ops.sc.rpc.health.HealthCheck;
import com.ops.sc.rpc.health.HealthGrpc;
import io.grpc.Status;
import io.grpc.StatusException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HealthCheckServiceImpl extends HealthGrpc.HealthImplBase {

    public static final String DELEGATE_SERVICE = "";

    private static Map<String, HealthCheck.HealthCheckResponse.ServingStatus> statusMap = Maps.newConcurrentMap();

    static {
        statusMap.put(DELEGATE_SERVICE, HealthCheck.HealthCheckResponse.ServingStatus.SERVING);
    }
    @Override
    public void check(HealthCheck.HealthCheckRequest request,
            io.grpc.stub.StreamObserver<HealthCheck.HealthCheckResponse> responseObserver) {
        HealthCheck.HealthCheckResponse.ServingStatus status = getStatus(request.getService());
        if (HealthCheck.HealthCheckResponse.ServingStatus.SERVING != status) {
            responseObserver.onError(
                    new StatusException(Status.NOT_FOUND.withDescription("Unknown service:" + request.getService())));
        } else {
            HealthCheck.HealthCheckResponse response = HealthCheck.HealthCheckResponse.newBuilder().setStatus(status)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    HealthCheck.HealthCheckResponse.ServingStatus getStatus(String service) {
        return statusMap.get(service);
    }
}
