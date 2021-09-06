package com.ops.sc.core.clone.context;


import com.ops.sc.core.clone.RollbackInfo;
import com.ops.sc.core.clone.RollbackItem;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImageContext implements Serializable {

    private static final long serialVersionUID = -4931509321334730252L;

    private List<RollbackItem> rollbackItemList = new ArrayList<>();

    private Object extValue;

}
