package com.ops.sc.common.model;

import com.ops.sc.common.store.ScBranchRecord;
import lombok.Data;

@Data
public class ExtensionBranchInfo extends ScBranchRecord {

    private String producerId;

    private String metaData;

    private String payload;


}
