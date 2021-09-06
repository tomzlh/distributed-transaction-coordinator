package com.ops.sc.ta.dao.impl;


import com.ops.sc.ta.dao.DaoSupport;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.ta.dao.LogDao;
import com.ops.sc.ta.trans.datasource.DatabaseResource;
import com.ops.sc.ta.trans.datasource.ScDataSource;
import com.ops.sc.ta.trans.support.ScDataSourceRecorder;
import com.ops.sc.ta.trans.xa.XADataSource;
import com.ops.sc.ta.trans.xa.XADataSourceRecorder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.XAConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component("scLogDao")
public class ScLogDaoImpl extends DaoSupport implements LogDao {

    private static final int LOG_BASE_PARAM_NUM = 5;

    private static final String SC_LOG = "sc_log";

    @Override
    public void insert(ScTransRecord scTransRecord, DatabaseResource databaseResource) throws SQLException {
        scTransRecord.setId(genID(databaseResource));
        doInsert(scTransRecord, databaseResource.getOriginalConnection());
    }

    @Override
    public void insert(ScTransRecord scTransRecord) throws SQLException {
        try {
            ScDataSource dataSource = ScDataSourceRecorder.getDefaultDataSource();
            scTransRecord.setId(genID(dataSource));

            try (Connection connection = dataSource.getOriginalConnection()) {
                doInsert(scTransRecord, connection);
            }
        }catch (Exception e){
            throw new SQLException(e);
        }
    }

    @Override
    public void insertXATrans(ScTransRecord scTransRecord) throws SQLException {
        XADataSource xaDataSource = XADataSourceRecorder.getDefaultXADataSource();
        XAConnection xaConnection = null;
        Connection connection = null;
        try {
            xaConnection = xaDataSource.getOriginXAConnection();
            connection = xaConnection.getConnection();
            doInsert(scTransRecord, connection);
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (xaConnection != null) {
                xaConnection.close();
            }
        }
    }

    @Override
    public ScTransRecord findInitiatorByTid(Long tid, Connection connection) throws SQLException {
        return findByTidAndBranchId(tid, tid, connection);
    }

    @Override
    public ScTransRecord findByTidAndBranchId(Long tid, Long branchId, Connection connection) throws SQLException {
        List<ScTransRecord> undoList = new ArrayList<>();
        ResultSet rs = null;
        String sql = "select * from " + SC_LOG + " where tid=? and branch_id = ?";
        try (PreparedStatement psm = connection.prepareStatement(sql)) {
            int index = 1;
            psm.setLong(index++, tid);
            psm.setLong(index, branchId);
            rs = psm.executeQuery();
            while (rs.next()) {
                undoList.add(makeScLog(rs));
            }

        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return CollectionUtils.isEmpty(undoList) ? null : undoList.get(0);
    }

    @Override
    public ScTransRecord findByTidAndBranchId(Long tid, Long branchId) throws SQLException {
        try {
            ScDataSource scDataSource = ScDataSourceRecorder.getDefaultDataSource();
            try (Connection connection = scDataSource.getOriginalConnection()) {
                return findByTidAndBranchId(tid, branchId, connection);
            }
        }catch (Exception e){
           throw new SQLException(e);
        }
    }

    @Override
    public int updateStatus(Long tid, Long branchId, Integer status, Connection connection) throws SQLException {
        String sql = "update " + SC_LOG + " set status =?  where xid =? and branch_id=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            int index = 1;
            pst.setInt(index++, status);
            pst.setLong(index++, tid);
            pst.setLong(index, branchId);
            return pst.executeUpdate();
        }
    }

    @Override
    public int updateStatus(Long tid, Long branchId, Integer status) throws SQLException {
        try{
         ScDataSource scDataSource = ScDataSourceRecorder.getDefaultDataSource();
         try (Connection connection = scDataSource.getOriginalConnection()) {
            return updateStatus(tid, branchId, status, connection);
         }
        }catch (Exception e){
            throw new SQLException(e);
        }
    }

    @Override
    public void delete(Long tid, Long branchId) throws SQLException {
        try{
        ScDataSource scDataSource = ScDataSourceRecorder.getDefaultDataSource();
        try (Connection connection = scDataSource.getOriginalConnection()) {
            delete(tid, branchId, connection);
        }
        }catch (Exception e){
            throw new SQLException(e);
        }
    }

    @Override
    public void delete(Long tid, Long branchId, Connection connection) throws SQLException {
        String sql = "DELETE FROM " + SC_LOG + " WHERE xid=? and branch_id=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            int index = 1;
            pst.setLong(index++, tid);
            pst.setLong(index, branchId);
            pst.executeUpdate();
        }
    }

    @Override
    public int updateInitiatorStatusByTid(Long tid, Integer status) throws SQLException {
        try{
        ScDataSource scDataSource = ScDataSourceRecorder.getDefaultDataSource();
        try (Connection connection = scDataSource.getOriginalConnection()) {
            return updateStatus(tid, tid, status, connection);
        }
        }catch (Exception e){
            throw new SQLException(e);
        }
    }

    @Override
    public int updateXAInitiatorStatusByTid(Long tid, Integer status) throws SQLException {
        XADataSource xaDataSource = XADataSourceRecorder.getDefaultXADataSource();
        XAConnection xaConnection = null;
        Connection connection = null;
        try {
            xaConnection = xaDataSource.getOriginXAConnection();
            connection = xaConnection.getConnection();
            return updateStatus(tid, tid, status, connection);
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (xaConnection != null) {
                xaConnection.close();
            }
        }
    }

    @Override
    protected String getTableName() {
        return SC_LOG;
    }

    private void doInsert(ScTransRecord scTransRecord, Connection connection) throws SQLException {
        int paramCount = LOG_BASE_PARAM_NUM;
        scTransRecord.setCreateTime(new Date());

        // 根据id和bf构造insert语句
        StringBuilder sb = new StringBuilder("insert into " + getTableName() + " (");
        if (scTransRecord.getId() != null) {
            sb.append("id, ");
            paramCount++;
        }

        sb.append("tid,branch_id,status,rollback_info,create_time) values (");
        String[] marks = new String[paramCount];
        Arrays.fill(marks, "?");
        sb.append(StringUtils.collectionToDelimitedString(Arrays.asList(marks), ","));
        sb.append(")");

        String sql = sb.toString();

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            int index = 1;
            if (scTransRecord.getId() != null) {
                pst.setObject(index++, scTransRecord.getId());
            }
            pst.setObject(index++, scTransRecord.getTid());
            //pst.setObject(index++, scTransRecord.getBranchId());
            pst.setObject(index++, scTransRecord.getStatus());
           // pst.setObject(index++, scTransRecord.getRollbackInfo());
            pst.setObject(index, scTransRecord.getCreateTime());
            pst.execute();
        }
    }

    private ScTransRecord makeScLog(ResultSet rs) throws SQLException {
        ScTransRecord scTransRecord = new ScTransRecord();
        scTransRecord.setId(rs.getLong("id"));
        scTransRecord.setStatus(rs.getInt("status"));
        scTransRecord.setTid(rs.getLong("tid"));
        scTransRecord.setCreateTime(rs.getDate("create_time"));
        return scTransRecord;
    }

}
