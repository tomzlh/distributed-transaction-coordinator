package com.ops.sc.core.clone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ImageInfo implements Serializable {

    private List<RollbackLine> lines = new ArrayList<>();

    public ImageInfo() {
    }

    public ImageInfo(List<RollbackLine> lines) {
        this.lines = lines;
    }

    public List<RollbackLine> getLines() {
        return lines;
    }

    public void setLines(List<RollbackLine> lines) {
        this.lines = lines;
    }
}
