package com.ops.sc.ta.service;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.enums.TransMode;
import com.ops.sc.common.reg.base.RegistryService;
import com.ops.sc.common.reg.zk.ZookeeperRegistryCenter;
import com.ops.sc.common.utils.InetUtil;
import com.ops.sc.core.resolver.ServicePropertyResolver;
import com.ops.sc.core.rest.RestControllerListener;
import com.ops.sc.core.rest.NettyServerFacade;
import com.ops.sc.core.rest.config.RpcServiceConfiguration;
import com.ops.sc.ta.config.TaConfiguration;
import com.ops.sc.ta.config.TransModeConfig;
import com.ops.sc.ta.config.TransModeConfigList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

@Service("transActuatorInitService")
public class TransActuatorInitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransActuatorInitService.class);

    @Autowired
    private RestControllerListener restControllerListener;

    @Resource
    private TransModeConfigList transModeConfigList;

    public void start(){
        ZookeeperRegistryCenter.getInstance().init();
        try {
            if(transModeConfigList.getModes()!=null) {
                for(TransModeConfig transModeConfig:transModeConfigList.getModes()) {
                    TransMode transMode=TransMode.valueOf(transModeConfig.getModeName());
                    if(transModeConfig.getPrepareMethod()!=null) {
                        String url = makeUrl(transModeConfig.getPrepareMethod());
                        ZookeeperRegistryCenter.getInstance().register(TaConfiguration.instance.getApplicationName(), transMode, Constants.PREPARE, url, RegistryService.HTTP);
                    }
                    if(transModeConfig.getCommitMethod()!=null){
                        String url = makeUrl(transModeConfig.getCommitMethod());
                        ZookeeperRegistryCenter.getInstance().register(TaConfiguration.instance.getApplicationName(), transMode, Constants.COMMIT, url, RegistryService.HTTP);
                    }
                    if(transModeConfig.getRollbackMethod()!=null){
                        String url = makeUrl(transModeConfig.getRollbackMethod());
                        ZookeeperRegistryCenter.getInstance().register(TaConfiguration.instance.getApplicationName(), transMode, Constants.CANCEL, url, RegistryService.HTTP);
                    }
                }
            }
            ZookeeperRegistryCenter.getInstance().register(new InetSocketAddress(InetUtil.getHostIp(), RpcServiceConfiguration.getServerPort()), RegistryService.HTTP);
            NettyServerFacade.getInstance().startup();
            LOGGER.info("Ta http netty server started!");
        }catch (Exception e){
            LOGGER.error("Transaction actuator http Netty server start failed!",e);
            System.exit(1);
        }
    }

    public void close(){
        ZookeeperRegistryCenter.getInstance().closeAll();
        NettyServerFacade.getInstance().shutdown();
    }

    private String makeUrl(String method) {
        return InetUtil.getHostIp() + ":" + RpcServiceConfiguration.getServerPort() + method;
    }


}
