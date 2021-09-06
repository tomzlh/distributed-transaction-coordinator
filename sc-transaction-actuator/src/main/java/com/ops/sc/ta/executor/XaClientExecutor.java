package com.ops.sc.ta.executor;

import com.ops.sc.common.bean.*;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.core.spi.TransHandlerSPI;

public interface XaClientExecutor extends TransHandlerSPI {

    ScResponseMessage prepare(ScRequestMessage branchPrepareRequest) throws ScClientException;

    ScResponseMessage confirm(ScRequestMessage branchCommitRequest) throws ScClientException;

    ScResponseMessage cancel(ScRequestMessage branchRollbackRequest) throws ScClientException;
}
