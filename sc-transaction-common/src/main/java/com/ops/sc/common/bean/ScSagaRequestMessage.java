
package com.ops.sc.common.bean;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Data
public class ScSagaRequestMessage {

    protected String businessId;

    protected long tid;

    protected long bid;

    protected String branchName;

    protected String transactionName;

    protected String transMode;

    protected Map<String, String> paramMap;

    protected List<String> returnParams;

    protected int messageType;

}
