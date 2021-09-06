package com.ops.sc.ta.clone.dto;

import com.ops.sc.ta.clone.enums.PrimaryKVType;
import net.sf.jsqlparser.expression.Expression;


public class PrimaryKVInfo {

    private PrimaryKVType pkValueType;

    private Expression pkValueExpression;

    public PrimaryKVInfo(PrimaryKVType pkValueType, Expression pkValueExpression) {
        this.pkValueType = pkValueType;
        this.pkValueExpression = pkValueExpression;
    }

    public PrimaryKVType getPkValueType() {
        return pkValueType;
    }

    public Expression getPkValueExpression() {
        return pkValueExpression;
    }
}
