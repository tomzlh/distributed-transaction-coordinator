package com.ops.sc.common.trans;



import java.util.List;

public interface BaseTwoPhaseTransaction<T> {

    /**
     * 事务保存
     *
     * @param baseInfo
     * @return
     */
    TransCommonResponse saveTransInfo(T baseInfo);


    TransCommonResponse saveTransInfo(List<T> baseInfos);
    /**
     * 事务提交
     *
     * @param baseInfo
     * @return
     */
    TransCommonResponse commit(T baseInfo);

    /**
     * 事务回滚
     *
     * @param baseInfo
     * @return
     */
    TransCommonResponse rollback(T baseInfo);
}
