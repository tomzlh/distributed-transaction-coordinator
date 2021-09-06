package com.ops.sc.common.reg.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionConnectionListener implements ConnectionStateListener {


    private static final Logger LOGGER = LoggerFactory.getLogger(SessionConnectionListener.class);

    private String path;
    private String data;

    public SessionConnectionListener(String path, String data) {
        this.path = path;
        this.data = data;
    }

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState){
        if(connectionState == ConnectionState.LOST){
            LOGGER.warn("zk session timeout");
            while(true){
                try {
                    if(curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()){
                        if(curatorFramework.checkExists().forPath(path)==null) {
                            curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(path, data.getBytes("UTF-8"));
                        }
                        LOGGER.info("reconnect the zk succeed!");
                        break;
                    }
                } catch (InterruptedException e) {
                    LOGGER.info("reconnect zk exception happened!",e);
                    break;
                } catch (Exception e){
                    LOGGER.info("reconnect zk exception happened!",e);
                    break;
                }
            }
        }
    }
}
