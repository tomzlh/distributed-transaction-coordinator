package com.ops.sc.core.clone;

import java.io.Serializable;


public class RollbackLineField implements Serializable {

    private String name;
    private String type;
    private Object value;

    public RollbackLineField() {
    }

    public RollbackLineField(String name, Object value) {
        this.name = name;
        this.value = value;
        if (value != null) {
            this.type = value.getClass().getName();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
