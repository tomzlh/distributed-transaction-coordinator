package com.ops.sc.core.clone;

import lombok.Data;

import java.io.Serializable;

/**
 * 回滚信息
 */
@Data
public class RollbackItem implements Serializable {

    private static final long serialVersionUID = -6238512855007028147L;

    private ImageInfo beforeImage;

    private ImageInfo afterImage;

    private String tableName;

    private String primaryKey;

    private String sqlSelect;

    private String sqlIdWhere;

    private String sqlRawWhere;

    private String sqlType;

    private String sql;
}

