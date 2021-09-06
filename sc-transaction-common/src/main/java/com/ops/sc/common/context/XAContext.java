package com.ops.sc.common.context;


import com.ops.sc.common.bean.XATid;
import lombok.Data;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import java.sql.Connection;

@Data
public class XAContext {
    private Connection connection;
    private XAResource xaResource;
    private XAConnection xaConnection;
    private String branchId;
    private Long xid;

    public XAContext(Connection connection, XAResource xaResource, XAConnection xaConnection,
            Long xid, String branchId) {
        this.connection = connection;
        this.xaResource = xaResource;
        this.xaConnection = xaConnection;
        this.xid = xid;
        this.branchId = branchId;
    }

}
