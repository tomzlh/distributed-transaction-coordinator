package com.ops.sc.common.heartbeat;

import java.util.Observable;


public class HeartBeatStatusPublisher extends Observable {

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        setChanged();
        notifyObservers(this);
    }
}
