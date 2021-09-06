package com.ops.sc.ta.executor;

import com.ops.sc.common.bean.*;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.core.spi.TransHandlerSPI;

public interface TccClientExecutor extends TransHandlerSPI {

      ScResponseMessage prepare(ScRequestMessage scRequestMessage) throws ScClientException;

      ScResponseMessage confirm(ScRequestMessage scRequestMessage) throws ScClientException;

      ScResponseMessage cancel(ScRequestMessage scRequestMessage) throws ScClientException;
}
