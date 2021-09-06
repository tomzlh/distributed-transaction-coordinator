package com.ops.sc.core.build;


import com.ops.sc.common.bean.LockContext;
import com.ops.sc.rpc.dto.BranchTransRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class LockContextBuilder {
    /**
     * lockContext转化为grpc request
     *
     * @param context
     * @return
     */
    public static BranchTransRequest.LockContext toLockContextRpcRequest(LockContext context) {
        if (context == null) {
            return null;
        }

        BranchTransRequest.LockContext.Builder contextBuilder = BranchTransRequest.LockContext.newBuilder();
        contextBuilder.setResourceId(context.getResourceId());
        contextBuilder.setRequireLock(context.getRequireLock());

        for (String table : context.getOperateMap().keySet()) {
            context.getOperateMap().get(table).toArray();
            BranchTransRequest.LockContext.KeyList.Builder keyListBuilder = BranchTransRequest.LockContext.KeyList
                    .newBuilder();
            keyListBuilder.addAllKeys(context.getOperateMap().get(table));

            contextBuilder.putOperateTableNames(table, keyListBuilder.build());
        }
        return contextBuilder.build();
    }

    /**
     * grpc request 转化为lockContext
     *
     * @return
     */
    public static LockContext rpcRequestToLockContext(BranchTransRequest.LockContext lockContext) {
        LockContext result = new LockContext();
        result.setResourceId(lockContext.getResourceId());
        result.setRequireLock(lockContext.getRequireLock());

        Map<String, Set<String>> map = new HashMap<>();

        for (String table : lockContext.getOperateTableNamesMap().keySet()) {
            BranchTransRequest.LockContext.KeyList keyList = lockContext.getOperateTableNamesMap().get(table);
            Set<String> keySet = new HashSet<>();

            for (int i = 0; i < keyList.getKeysCount(); i++) {
                keySet.add(keyList.getKeys(i));
            }
            map.put(table, keySet);
        }
        result.setOperateMap(map);
        return result;
    }
}
