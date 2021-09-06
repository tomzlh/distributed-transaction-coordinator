
package com.ops.sc.server.recorder;

import com.ops.sc.common.enums.Participant;
import com.ops.sc.common.bean.TransRegisterRequest;
import com.ops.sc.common.reg.base.RegistryService;
import com.ops.sc.common.reg.zk.ZookeeperRegistryCenter;
import io.netty.channel.Channel;
import io.netty.util.internal.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.stream.Collectors.toList;


public class TaServiceRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaServiceRecorder.class);


    private static final ConcurrentMap<String,ConcurrentMap<String,RestRpcContext>> TA_SERVICES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String,RestRpcContext> UNIQUE_SERVICES = new ConcurrentHashMap<>();


    public static boolean isRegistered(String url) {
        return UNIQUE_SERVICES.containsKey(url);
    }



    private static String getClientId(String applicationId, String url) {
        return applicationId + ":" + url;
    }



    private static RestRpcContext buildChannelContext(Participant clientRole,
                                                       String transServiceGroup, String url) {
        RestRpcContext context = new RestRpcContext();
        context.setClientRole(clientRole);
        context.setClientId(getClientId(transServiceGroup, url));
        context.setTransactionServiceGroup(transServiceGroup);
        context.setUrl(url);
        return context;
    }




    public static void registerClient(String uniqueName, String url) {
        RestRpcContext restRpcContext;
        if (!UNIQUE_SERVICES.containsKey(url)) {
            restRpcContext = buildChannelContext(Participant.TA,
                    uniqueName, url);
            UNIQUE_SERVICES.putIfAbsent(url,restRpcContext);
        } else {
            restRpcContext = UNIQUE_SERVICES.get(url);
        }
        ConcurrentMap<String, RestRpcContext> urlMap=TA_SERVICES.get(uniqueName);
        if(urlMap==null){
             urlMap = new ConcurrentHashMap<>();
             urlMap.putIfAbsent(url,restRpcContext);
        }
        else {
             RestRpcContext rpcContext=urlMap.get(url);
             if(rpcContext==null){
                 urlMap.putIfAbsent(url,restRpcContext);
             }
        }
        TA_SERVICES.putIfAbsent(uniqueName,urlMap);
    }

    /*private static void updateChannelsInfo(String clientIp, String applicationId) {
        ConcurrentMap<Integer, NettyRpcContext> sourcePortMap = TA_CHANNELS.get(applicationId).get(clientIp);
        for (ConcurrentMap.Entry<String, ConcurrentMap<String, ConcurrentMap<Integer,
                NettyRpcContext>>> rmChannelEntry : TA_CHANNELS.entrySet()) {
            if (rmChannelEntry.getKey().equals(resourceId)) { continue; }
            ConcurrentMap<String, ConcurrentMap<Integer,
                    NettyRpcContext>> applicationIdMap = rmChannelEntry.getValue();
            if (!applicationIdMap.containsKey(applicationId)) { continue; }
            ConcurrentMap<Integer, NettyRpcContext> clientIpMap = applicationIdMap.get(applicationId);
            if (!clientIpMap.containsKey(clientIp)) { continue; }
            for (ConcurrentMap.Entry<Integer, NettyRpcContext> clientMapEntry : clientIpMap.entrySet()) {
                Integer port = clientMapEntry.getKey();
                if (!sourcePortMap.containsKey(port)) {
                    NettyRpcContext nettyRpcContext = clientMapEntry.getValue();
                    sourcePortMap.put(port, nettyRpcContext);
                    nettyRpcContext.addIntoResourceManagerChannels(resourceId, port);
                }
            }
        }
    }*/


    public static String getClientUrl(String branchName, String operateName) {
        String uniqueName=branchName+"_"+operateName;
        ConcurrentMap<String,RestRpcContext> urlMap = TA_SERVICES.get(uniqueName);
        if (urlMap == null || urlMap.isEmpty()) {
            List<String> addressList=ZookeeperRegistryCenter.getInstance().lookup(branchName, RegistryService.HTTP);
            if(addressList!=null){
                for(String address:addressList) {
                    String[] addresses=splitAddress(address);
                    if(addresses.length==2) {
                        String registerName = branchName + "_" + addresses[0];
                        registerClient(registerName, toUrl(addresses[1]));
                    }
                }
                return selectOneUrl(TA_SERVICES,uniqueName);
            }
        }
        List<String> urls=urlMap.keySet().stream().collect(toList());
        String resultUrl = urls.get(ThreadLocalRandom.current().nextInt(urls.size()));
        return resultUrl;
    }

    public static String[] splitAddress(String address){
        return address.split("#");
    }

    public static String toUrl(String address){
        return "http://"+address.replaceAll("@","/");
    }



    public static String selectOneUrl(ConcurrentMap<String, ConcurrentMap<String,
            RestRpcContext>> applicationUrlMap, String applicationName) {
        String chosenUrl = null;
        ConcurrentMap<String, RestRpcContext> urlMap = applicationUrlMap.get(applicationName);
        if (urlMap == null || urlMap.isEmpty()) {
            return chosenUrl;
        }
        List<String> urls=urlMap.keySet().stream().collect(toList());
        chosenUrl = urls.get(ThreadLocalRandom.current().nextInt(urls.size()));
        return chosenUrl;
    }

    /**
     * get rm channels
     *
     * @return
     */
    public static ConcurrentMap<String, ConcurrentMap<String, RestRpcContext>> getTaChannels() {
        return TA_SERVICES;
    }

}
