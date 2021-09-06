package com.ops.sc.ta.trans.datasource;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public abstract class BasePreparedStatementProxy extends BaseStatementProxy implements PreparedStatement {
    protected Map<Integer, Object> paramMap = new TreeMap<>();
    protected String sql;

    public BasePreparedStatementProxy(PreparedStatement preparedStatement, String sql, ScDataSource scDataSource,
            DatabaseResource databaseResource) {
        super(preparedStatement, scDataSource, databaseResource);
        this.sql = sql;
    }

    public PreparedStatement getPreparedStatement() {
        return (PreparedStatement) this.statement;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        getPreparedStatement().setNull(parameterIndex, sqlType);
        paramMap.put(parameterIndex, null);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        getPreparedStatement().setBoolean(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        getPreparedStatement().setByte(parameterIndex, x);
        paramMap.put(parameterIndex, x);

    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        getPreparedStatement().setShort(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        getPreparedStatement().setInt(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        getPreparedStatement().setLong(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        getPreparedStatement().setFloat(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        getPreparedStatement().setDouble(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        getPreparedStatement().setBigDecimal(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        getPreparedStatement().setString(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        getPreparedStatement().setBytes(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        getPreparedStatement().setDate(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        getPreparedStatement().setTime(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        getPreparedStatement().setTimestamp(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        getPreparedStatement().setObject(parameterIndex, x);
        paramMap.put(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        getPreparedStatement().setObject(parameterIndex, x, targetSqlType);
        paramMap.put(parameterIndex, x);
    }

    // 不支持-begin
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        getPreparedStatement().setAsciiStream(parameterIndex, x, length);
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        getPreparedStatement().setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        getPreparedStatement().setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        getPreparedStatement().setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void addBatch() throws SQLException {
        getPreparedStatement().addBatch();
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        getPreparedStatement().setRef(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        getPreparedStatement().setBlob(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        getPreparedStatement().setClob(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        getPreparedStatement().setArray(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        getPreparedStatement().setDate(parameterIndex, x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        getPreparedStatement().setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        getPreparedStatement().setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        getPreparedStatement().setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        getPreparedStatement().setURL(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        getPreparedStatement().setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        getPreparedStatement().setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        getPreparedStatement().setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        getPreparedStatement().setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        getPreparedStatement().setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        getPreparedStatement().setRowId(parameterIndex, x);
    }

    @Override
    public int[] executeBatch() throws SQLException {
        // TODO support?
        return getPreparedStatement().executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getPreparedStatement().getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return getPreparedStatement().getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return getPreparedStatement().getGeneratedKeys();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        getPreparedStatement().setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        getPreparedStatement().setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        getPreparedStatement().setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        getPreparedStatement().setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        getPreparedStatement().setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        getPreparedStatement().setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        getPreparedStatement().setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        getPreparedStatement().setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        getPreparedStatement().setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        getPreparedStatement().setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        getPreparedStatement().setClob(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        getPreparedStatement().setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        getPreparedStatement().setNClob(parameterIndex, reader);
    }
    // 不支持-end

    @Override
    public void clearParameters() throws SQLException {
        getPreparedStatement().clearParameters();
        paramMap.clear();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return getPreparedStatement().getMetaData();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return getPreparedStatement().getParameterMetaData();
    }

    @Override
    public void close() throws SQLException {
        getPreparedStatement().close();
        paramMap.clear();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return getPreparedStatement().getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        getPreparedStatement().setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return getPreparedStatement().getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        getPreparedStatement().setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        getPreparedStatement().setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return getPreparedStatement().getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        getPreparedStatement().setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        getPreparedStatement().cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return getPreparedStatement().getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        getPreparedStatement().clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        getPreparedStatement().setCursorName(name);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return getPreparedStatement().getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return getPreparedStatement().getUpdateCount();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return getPreparedStatement().getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return getPreparedStatement().isClosed();
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return getPreparedStatement().isPoolable();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        getPreparedStatement().setPoolable(poolable);
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        getPreparedStatement().closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return getPreparedStatement().isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getPreparedStatement().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getPreparedStatement().isWrapperFor(iface);
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return getPreparedStatement().getMoreResults();
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return getPreparedStatement().getFetchDirection();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        getPreparedStatement().setFetchDirection(direction);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return getPreparedStatement().getFetchSize();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        getPreparedStatement().setFetchSize(rows);
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return getPreparedStatement().getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return getPreparedStatement().getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        // TODO support?
        getPreparedStatement().addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        getPreparedStatement().clearBatch();
    }

    protected String getFullSql() {
        if (paramMap == null || paramMap.size() == 0) {
            return sql;
        }

        int size = paramMap.size();
        Object[] params = new Object[size];
        paramMap.values().toArray(params);
        for (int i = 0; i < size; i++) {
            Object value = params[i];
            if (value instanceof Date || value instanceof Timestamp || value instanceof String) {
                params[i] = "'" + value + "'";
            } else if (value instanceof Boolean) {
                params[i] = (Boolean) value ? 1 : 0;
            }
        }

        return String.format(sql.replaceAll("\\?", "%s"), params);
    }
}
