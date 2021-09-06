package com.ops.sc.core.clone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RollbackLine implements Serializable {

    private List<RollbackLineField> fields = new ArrayList<>();

    public List<RollbackLineField> getFields() {
        return fields;
    }

    public void setFields(List<RollbackLineField> fields) {
        this.fields = fields;
    }
}
