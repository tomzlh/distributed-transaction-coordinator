package com.ops.sc.ta.clone.rollback;

import java.io.Serializable;
import java.util.Map;

public class RowInfoMapWrapper implements Serializable, Comparable<RowInfoMapWrapper> {

    private String pkValue;

    private Map<String, Object> rowInfoMap;

    public RowInfoMapWrapper() {
    }

    RowInfoMapWrapper(String pkValue, Map<String, Object> rowInfoMap) {
        this.pkValue = pkValue;
        this.rowInfoMap = rowInfoMap;
    }

    @Override
    public int compareTo(RowInfoMapWrapper o) {
        return pkValue.compareTo(o.pkValue);
    }

    public String getPkValue() {
        return pkValue;
    }

    public void setPkValue(String pkValue) {
        this.pkValue = pkValue;
    }

    public Map<String, Object> getRowInfoMap() {
        return rowInfoMap;
    }

    public void setRowInfoMap(Map<String, Object> rowInfoMap) {
        this.rowInfoMap = rowInfoMap;
    }
}
