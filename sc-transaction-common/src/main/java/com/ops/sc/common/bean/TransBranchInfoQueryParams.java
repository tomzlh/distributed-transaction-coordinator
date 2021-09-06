package com.ops.sc.common.bean;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TransBranchInfoQueryParams {

    private Long id; // 根据主键ID查询

    private List<Integer> statusList; // 根据状态查询

    private Long tid;

    private String branchId;

    private Page page;


}
