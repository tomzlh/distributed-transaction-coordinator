package com.ops.sc.ta.clone.dto;

import com.ops.sc.ta.clone.enums.PrimaryKVType;
import com.ops.sc.ta.clone.enums.SqlType;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;


public class SqlInfo implements Serializable {
    private Set<String> pkValueSet;
    private List<String> columnNameList;
    private String tableName;
    private String primaryKey;
    private String sqlWhere;
    private String sql;
    private String sqlType;
    private Boolean forUpdate = true;
    private Statement sqlParseStatement;

    public Statement getSqlParseStatement() {
        return sqlParseStatement;
    }

    public void setSqlParseStatement(Statement sqlParseStatement) {
        this.sqlParseStatement = sqlParseStatement;
    }

    public Set<String> getPrimaryKeyValueSet() {
        return pkValueSet;
    }

    public void setPrimaryKeyValueSet(Set<String> primaryKeyValueSet) {
        this.pkValueSet = primaryKeyValueSet;
    }

    public List<String> getColumnNameList() {
        return columnNameList;
    }

    public void setColumnNameList(List<String> columnNameList) {
        this.columnNameList = columnNameList;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getSqlWhere() {
        return sqlWhere;
    }

    public void setSqlWhere(String sqlWhere) {
        this.sqlWhere = sqlWhere;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    private List<String> getColumnNameListContainsPrimaryKey() {
        if (columnNameList.contains("*")) {
            return columnNameList;
        }

        if (columnNameList.contains(primaryKey)) {
            return columnNameList;
        }

        List<String> result = new ArrayList<>(columnNameList);
        result.add(primaryKey);
        return result;
    }

    private List<String> addBalanceField(List<String> columnList, String bf) {
        if (columnList.contains("*")) {
            return columnList;
        }

        if (columnList.contains(bf)) {
            return columnList;
        }

        columnList.add(bf);
        return columnList;
    }


    public String getSqlSelect() {
        List<String> columns = getColumnNameListContainsPrimaryKey();
        return "select " + StringUtils.collectionToDelimitedString(columns, ",") + " from " + tableName;
    }



    public String getSqlWherePrimaryKey() {
        if (CollectionUtils.isEmpty(pkValueSet)) {
            return sqlWhere;
        }
        List<String> primaryKeyValueList = new ArrayList<>(pkValueSet);
        StringBuilder sqlBuilder = new StringBuilder(" where ");
        sqlBuilder.append(primaryKey);
        if (primaryKeyValueList.size() == 1) {
            sqlBuilder.append("=").append(primaryKeyValueList.get(0));
        } else {
            sqlBuilder.append(" in (").append(StringUtils.collectionToDelimitedString(primaryKeyValueList, ","))
                    .append(") ");
        }
        return sqlBuilder.toString();
    }

    public Boolean getForUpdate() {
        return forUpdate;
    }

    public void setForUpdate(Boolean forUpdate) {
        this.forUpdate = forUpdate;
    }

    public Set<PrimaryKVInfo> parseSqlToGetPrimaryKeyValueInfo() {
        if (SqlType.INSERT.name().equals(sqlType)) {
            ItemsList itemList = ((Insert) sqlParseStatement).getItemsList();
            List<ExpressionList> expressionListList = new ArrayList<>();
            if (ExpressionList.class.isInstance(itemList)) {
                expressionListList.add((ExpressionList) itemList);
            }
            if (MultiExpressionList.class.isInstance(itemList)) {
                expressionListList.addAll(((MultiExpressionList) itemList).getExprList());
            }
            Optional<String> pk = columnNameList.stream().filter(c -> c.equalsIgnoreCase(primaryKey)).findAny();
            int indexOfPrimaryKey = pk.isPresent() ? columnNameList.indexOf(pk.get()) : -1;
            Set<PrimaryKVInfo> pkValueInfoSet = new HashSet<>();
            if (indexOfPrimaryKey >= 0) {
                // sql语句中含有对应的主键值
                for (ExpressionList expressionList : expressionListList) {
                    Expression primaryKeyValue = expressionList.getExpressions().get(indexOfPrimaryKey);
                    if (!(primaryKeyValue instanceof NullValue)) {
                        if (primaryKeyValue instanceof Column) {
                            // todo 暂时适配oracle
                            pkValueInfoSet.add(new PrimaryKVInfo(PrimaryKVType.SEQ, primaryKeyValue));
                        } else {
                            pkValueInfoSet.add(new PrimaryKVInfo(PrimaryKVType.VALUE, primaryKeyValue));
                        }
                    }
                }
            }
            return pkValueInfoSet;
        }
        throw new IllegalStateException("invoke parseSqlToGetPrimaryKeyValueInfo not in insert sql");
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
