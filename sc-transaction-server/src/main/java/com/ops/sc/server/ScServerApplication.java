package com.ops.sc.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.ops.sc.common.reg.base.RegistryService;
import com.ops.sc.common.reg.zk.ZookeeperRegistryCenter;
import com.ops.sc.common.utils.InetUtil;
import com.ops.sc.server.conf.ServerConfiguration;
import com.ops.sc.server.state.HealthCheckServiceImpl;
import com.ops.sc.core.rpc.RemoteCallService;
import com.ops.sc.core.rpc.RpcCallBackService;
import com.ops.sc.core.rpc.pb.RpcServer;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import com.google.common.collect.Sets;
import com.ops.sc.server.service.TransactionManagerService;


@EnableAutoConfiguration
@ComponentScan({"com.ops.sc.common","com.ops.sc.mybatis.datasource","com.ops.sc.core","com.ops.sc.server" })
@ImportResource("classpath:applicationContext.xml")
@MapperScan(basePackages = "com.ops.sc.mybatis.mapper")
@SpringBootApplication
public class ScServerApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScServerApplication.class);


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ScServerApplication.class, args);
        ZookeeperRegistryCenter.getInstance().init();
        initRPC(context);
    }


    private static void initRPC(ConfigurableApplicationContext context) {
        final RpcServer rpcServer = context.getBean(RpcServer.class);
        TransactionManagerService transactionManagerService = context.getBean(TransactionManagerService.class);
        RpcCallBackService rpcCallBackService = context.getBean(RpcCallBackService.class);
        RemoteCallService innerCallService = context.getBean(RemoteCallService.class);
        HealthCheckServiceImpl healthCheckService = context.getBean(HealthCheckServiceImpl.class);
        int rpcServerPort = ServerConfiguration.instance.getRpcServerPort();
        rpcServer.setPort(rpcServerPort);
        rpcServer.setServices(Sets.newHashSet(transactionManagerService, rpcCallBackService, innerCallService,healthCheckService));
        try {
            rpcServer.start();
            ZookeeperRegistryCenter.getInstance().register(new InetSocketAddress(InetUtil.getHostIp(),rpcServerPort),RegistryService.GPRC);
            LOGGER.info("SC RPC Server started, listening on port: {}", rpcServerPort);
            rpcServer.blockUntilShutdown();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.err.println("Shutting down server");
                rpcServer.stop();
                ZookeeperRegistryCenter.getInstance().close();
            }));
        } catch (IOException e) {
            LOGGER.error("SC RPC Server start failed!");
            System.exit(1);
        } catch (InterruptedException e) {
            LOGGER.error("SC RPC Server stop failed!");
            System.exit(1);
        }
    }


}
