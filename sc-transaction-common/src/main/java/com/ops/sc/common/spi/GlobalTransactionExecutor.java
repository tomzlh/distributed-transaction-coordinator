package com.ops.sc.common.spi;

import com.ops.sc.common.bean.GlobalTransRequestBean;
import com.ops.sc.common.bean.GlobalTransResponseBean;


public interface GlobalTransactionExecutor {

       GlobalTransResponseBean execute(GlobalTransRequestBean globalTransRequestBean);

       String getTransName();
}
