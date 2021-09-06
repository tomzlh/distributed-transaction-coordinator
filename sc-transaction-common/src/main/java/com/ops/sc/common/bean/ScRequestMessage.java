
package com.ops.sc.common.bean;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ScRequestMessage  {

    protected String businessId;

    protected long tid;

    /**
     * The Branch id.
     */
    protected long branchId;

    //xa模式下使用
    protected String dataSource;

    protected String branchName;

    protected String transactionName;

    protected String transMode;

    protected Map<String, String> paramMap;

    protected List<String> returnParams;

    protected int messageType;

}
