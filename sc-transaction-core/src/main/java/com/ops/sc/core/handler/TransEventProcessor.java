
package com.ops.sc.core.handler;

import com.ops.sc.common.bean.ScResponseMessage;
import com.ops.sc.core.spi.TransProcessorSPI;


public interface TransEventProcessor extends TransProcessorSPI {


    void dealWith(ScResponseMessage scResponseMessage) throws Exception;

}
