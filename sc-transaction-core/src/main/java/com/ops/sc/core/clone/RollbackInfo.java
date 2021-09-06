package com.ops.sc.core.clone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class RollbackInfo implements Serializable {
    private List<RollbackItem> info = new ArrayList<>();

    public List<RollbackItem> getInfo() {
        return info;
    }

    public void setInfo(List<RollbackItem> info) {
        this.info = info;
    }
}
