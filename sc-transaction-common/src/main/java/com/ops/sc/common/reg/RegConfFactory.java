package com.ops.sc.common.reg;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.lb.LoadBalance;
import com.ops.sc.common.config.ConfigService;
import com.ops.sc.common.config.ConfigType;
import com.ops.sc.common.config.PropertyResolver;
import com.ops.sc.common.reg.base.RegistryService;
import com.ops.sc.common.reg.base.RegistryType;
import com.ops.sc.common.lb.LoadBalanceType;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RegConfFactory {

    private static RegConfFactory instance = SingleInstanceHolder.instance;

    private static RegistryType registryType = null;

    private static ConfigType configType = null;

    private static LoadBalanceType loadBalanceType = null;

    private static Map<String,RegistryService> registryServiceHashMap=new ConcurrentHashMap<>(8);

    private static Map<String,ConfigService> configServiceHashMap=new ConcurrentHashMap<>(8);

    private static Map<String, LoadBalance>  loadBalanceHashMap=new ConcurrentHashMap<>(8);


    public static RegConfFactory getInstance(){
        return instance;
    }

    public void registerRegistry(RegistryType registryType,RegistryService registryService){
        registryServiceHashMap.put(registryType.name(),registryService);
    }

    public void registerConfiguration(ConfigType configType, ConfigService configService){
        configServiceHashMap.put(configType.name(),configService);
    }


    public void registerLB(LoadBalanceType loadBalanceType, LoadBalance loadBalance){
        loadBalanceHashMap.put(loadBalanceType.name(),loadBalance);
    }


    public  RegistryService getRegistryService(){
        if(registryType==null) {
            String registryTypeName = PropertyResolver.getINSTANCE().getValue(
                    Constants.FILE_ROOT_REGISTRY + Constants.FILE_CONFIG_SPLIT_CHAR+Constants.FILE_MIDDLE_REGISTRY+Constants.FILE_CONFIG_SPLIT_CHAR
                            + Constants.FILE_ROOT_TYPE);
            try {
                registryType = RegistryType.getType(registryTypeName);
            } catch (Exception exx) {
                throw new RuntimeException("not support registry type: " + registryTypeName);
            }
        }
        return registryServiceHashMap.get(registryType.name());
    }

    public  ConfigService getConfigService() {
        if(configType==null) {
            String configTypeName = PropertyResolver.getINSTANCE().getValue(
                    Constants.FILE_ROOT_CONFIG + Constants.FILE_CONFIG_SPLIT_CHAR
                            + Constants.FILE_ROOT_TYPE);

            if (StringUtils.isBlank(configTypeName)) {
                throw new RuntimeException("config type can not be null");
            }
            configType = ConfigType.getType(configTypeName);
        }
        return configServiceHashMap.get(configType.name());
    }

    public LoadBalance getLoadBalance() {
        if(loadBalanceType==null) {
            String loadBalanceTypeName = PropertyResolver.getINSTANCE().getValue(
                    Constants.LOAD_BALANCE_TYPE);

            if (StringUtils.isBlank(loadBalanceTypeName)) {
                PropertyResolver.getINSTANCE().getValue(
                        Constants.LOAD_BALANCE_TYPE);
            }
            loadBalanceType = LoadBalanceType.getType(LoadBalanceType.RANDOM.name());
        }
        return loadBalanceHashMap.get(loadBalanceType.name());
    }



    private static class SingleInstanceHolder {
        private static RegConfFactory instance = new RegConfFactory();
    }
}
