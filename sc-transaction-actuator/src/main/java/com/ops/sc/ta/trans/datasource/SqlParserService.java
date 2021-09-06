package com.ops.sc.ta.trans.datasource;

import com.google.common.collect.Lists;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.ta.clone.dto.SqlInfo;
import com.ops.sc.ta.clone.enums.SqlType;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SqlParserService {

    private static Statement parseSql(String sql) throws ScClientException{
        try {
            return CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            throw new ScClientException(ClientErrorCode.UNSUPPORTED_SQL, "Not Support sql=" + sql, e);
        }
    }

    private static List<String> getColumnNameList(List<Column> columnList) {
        List<String> columnNameList = new ArrayList<>();
        for (Column column : columnList) {
            columnNameList.add(column.getName(true));
        }
        return columnNameList;
    }

    private static SqlInfo parseSelectSQL(String sql)  throws ScClientException{
        SqlInfo result = new SqlInfo();
        result.setSql(sql);
        result.setSqlType(SqlType.SELECT.name());
        Select select = (Select) parseSql(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);
        if (tableList.size() > 1) {
            throw new ScClientException(ClientErrorCode.UNSUPPORTED_SQL, "Not Support join select sql=" + sql);
        }
        result.setTableName(tableList.get(0));
        PlainSelect selectBody = (PlainSelect) select.getSelectBody();
        Expression where = selectBody.getWhere();
        if (where != null) {
            result.setSqlWhere(" where " + selectBody.getWhere().toString());
        }
        List<String> columnNameList = Lists.newArrayList();
        for (SelectItem selectItem : selectBody.getSelectItems()) {
            columnNameList.add(selectItem.toString());
        }
        result.setColumnNameList(columnNameList);
        result.setForUpdate(selectBody.isForUpdate());
        return result;
    }

    private static SqlInfo parseInsertSQL(String sql)  throws ScClientException{
        SqlInfo result = new SqlInfo();
        result.setSql(sql);
        result.setSqlType(SqlType.INSERT.name());

        Insert insert = (Insert) parseSql(sql);
        result.setSqlParseStatement(insert);
        String tableName = insert.getTable().getName();
        result.setTableName(tableName);
        result.setColumnNameList(getColumnNameList(insert.getColumns()));

        return result;
    }

    private static SqlInfo parseUpdateSQL(String sql)  throws ScClientException{
        SqlInfo result = new SqlInfo();
        result.setSql(sql);
        result.setSqlType(SqlType.UPDATE.name());

        Update update = (Update) parseSql(sql);
        List<Table> updateTableList = update.getTables();
        if (updateTableList.size() > 1) {
            throw new ScClientException(ClientErrorCode.UNSUPPORTED_SQL, "Not Support join update sql=" + sql);
        }
        result.setTableName(updateTableList.get(0).toString());
        result.setColumnNameList(getColumnNameList(update.getColumns()));

        Expression where = update.getWhere();
        if (where != null) {
            result.setSqlWhere(" where " + where.toString());
        }
        return result;
    }

    private static SqlInfo parseDeleteSQL(String sql)  throws ScClientException{
        SqlInfo result = new SqlInfo();
        result.setSql(sql);
        result.setSqlType(SqlType.DELETE.name());

        Delete delete = (Delete) parseSql(sql);
        result.setTableName(delete.getTable().getName());
        result.setColumnNameList(Collections.singletonList("*"));
        Expression where = delete.getWhere();
        if (where != null) {
            result.setSqlWhere(" where " + where.toString());
        }
        return result;
    }

    public static SqlInfo parseSqlInfo(String sql)  throws ScClientException{
        if (isSelectSQL(sql)) {
            return parseSelectSQL(sql);
        }
        if (isInsertSQL(sql)) {
            return parseInsertSQL(sql);
        }
        if (isUpdateSQL(sql)) {
            return parseUpdateSQL(sql);
        }
        if (isDeleteSQL(sql)) {
            return parseDeleteSQL(sql);
        }
        throw new ScClientException(ClientErrorCode.UNSUPPORTED_SQL, "Not Support sql=" + sql);
    }

    private static boolean isSelectSQL(String sql) {
        return sql.toLowerCase().trim().startsWith("select");
    }

    private static boolean isInsertSQL(String sql) {
        return sql.toLowerCase().trim().startsWith("insert");
    }

    private static boolean isUpdateSQL(String sql) {
        return sql.toLowerCase().trim().startsWith("update");
    }

    private static boolean isDeleteSQL(String sql) {
        return sql.toLowerCase().trim().startsWith("delete");
    }
}
