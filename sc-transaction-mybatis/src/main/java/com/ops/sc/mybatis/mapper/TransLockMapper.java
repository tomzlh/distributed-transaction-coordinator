package com.ops.sc.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ops.sc.common.model.TransLock;


public interface TransLockMapper {

    void deleteByTidAndBranchId(@Param("tid") Long tid, @Param("branchId") Long branchId);

    void insert(TransLock lock);

    void batchInsert(@Param("lockList") List<TransLock> lockList);

    List<TransLock> queryTransLockList(@Param("tableName") String tableName, @Param("kvList") List<String> kvList);

    List<TransLock> queryTransLockListByTid(@Param("tid") Long tid);

}
