package com.ops.sc.core.rpc.pb;


import com.ops.sc.common.exception.RequestException;
import com.ops.sc.core.util.ApplicationUtils;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.Setter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Executor;

@Service
public class RpcServer {
    @Setter
    private int port;

    private Server server;
    @Setter
    private Set<BindableService> services;


    public void start() throws IOException {
        ServerBuilder serverBuilder = ServerBuilder.forPort(port);
        for (BindableService service : services) {
            serverBuilder.addService(service);
        }
        server = serverBuilder.executor(ApplicationUtils.getBean("rpcServerTask", Executor.class)).build().start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server");
            RpcServer.this.stop();
        }));
    }



    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public int getPort() {
        return port;
    }
}
