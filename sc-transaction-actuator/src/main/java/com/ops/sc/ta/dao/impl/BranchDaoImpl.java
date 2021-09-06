package com.ops.sc.ta.dao.impl;


import com.ops.sc.common.model.BranchInfo;
import com.ops.sc.ta.dao.BranchDao;
import com.ops.sc.ta.trans.datasource.ScDataSource;
import com.ops.sc.ta.trans.support.ScDataSourceRecorder;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class BranchDaoImpl implements BranchDao {

    private static final String TRANS_BRANCH = "trans_branch";

    private static final String INSERT_BRANCH_SQL = "INSERT INTO " + TRANS_BRANCH
            + "(tid,branch_id,parent_id,status,branch_name,try_timeout,try_timeout_type,retry_count,instance_name,trans_type,params,resource_id,create_time,modify_time)"
            + " VALUES (?, ?, ?, ?, ?,?, ?, ?, ?, ?,?, ?, ?,?)";

    @Override
    public void insert(BranchInfo branchInfo) throws SQLException {
        try {
            ScDataSource dataSource = ScDataSourceRecorder.getDefaultDataSource();
            try (Connection connection = dataSource.getOriginalConnection()) {
                doInsert(branchInfo, connection);
            }
        }catch (Exception e){
            throw new SQLException(e);
        }
    }

    private void doInsert(BranchInfo branchInfo, Connection connection) throws SQLException {
        int paramIndex = 1;
        try (PreparedStatement pst = connection.prepareStatement(INSERT_BRANCH_SQL)) {
            pst.setLong(paramIndex++, branchInfo.getTid());
            pst.setLong(paramIndex++, branchInfo.getBid());
            pst.setString(paramIndex++, branchInfo.getParentId());
            pst.setInt(paramIndex++, branchInfo.getStatus());
            pst.setString(paramIndex++, branchInfo.getTransactionName());
            pst.setLong(paramIndex++, branchInfo.getTimeout());
            pst.setInt(paramIndex++, branchInfo.getTimeoutType());
            pst.setInt(paramIndex++, branchInfo.getRetryCount());
            pst.setInt(paramIndex++, branchInfo.getTransType());
            pst.setBytes(paramIndex++, branchInfo.getParams());
            pst.setString(paramIndex++, branchInfo.getResourceId());
            pst.setObject(paramIndex++, branchInfo.getCreateTime());
            pst.setObject(paramIndex, branchInfo.getModifyTime());
            pst.executeUpdate();
        }
    }

    @Override
    public BranchInfo findByTidAndBranchId(Long tid, Long branchId) throws SQLException {
        String sql = "select * from " + TRANS_BRANCH + " where tid=? and branch_id = ?";
        List<BranchInfo> transBranchList = new ArrayList<>();
        ResultSet rs = null;
        int paramIndex = 1;
        try {
            ScDataSource dataSource = ScDataSourceRecorder.getDefaultDataSource();
            try (Connection connection = dataSource.getOriginalConnection()) {
                try (PreparedStatement pst = connection.prepareStatement(sql)) {
                    pst.setLong(paramIndex++, tid);
                    pst.setLong(paramIndex, branchId);
                    rs = pst.executeQuery();
                    while (rs.next()) {
                        transBranchList.add(buildTransBranch(rs));
                    }
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
            }
        }catch (Exception e){
            throw new SQLException(e);
        }
        return CollectionUtils.isEmpty(transBranchList) ? null : transBranchList.get(0);
    }

    @Override
    public List<BranchInfo> findByStatusList(List<Integer> statusList) throws SQLException {
        StringBuilder sb = new StringBuilder("select * from " + TRANS_BRANCH + " where status in ( ");
        int length = statusList.size();
        for (int i = 0; i < length; i++) {
            sb.append("?");
            if (i != (length - 1)) {
                sb.append(", ");
            }
        }
        sb.append(" )");
        List<BranchInfo> transBranchList = new ArrayList<>();
        ResultSet rs = null;
        try {
            ScDataSource dataSource = ScDataSourceRecorder.getDefaultDataSource();
            try (Connection connection = dataSource.getOriginalConnection()) {
                try (PreparedStatement pst = connection.prepareStatement(sb.toString())) {
                    for (int i = 0; i < length; i++) {
                        pst.setInt(i + 1, statusList.get(i));
                    }
                    rs = pst.executeQuery();
                    while (rs.next()) {
                        transBranchList.add(buildTransBranch(rs));
                    }
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
            }
        }catch (Exception e){
            throw new SQLException(e);
        }
        return transBranchList;
    }


    private BranchInfo buildTransBranch(ResultSet rs) throws SQLException {
        BranchInfo branchInfo = new BranchInfo();
        branchInfo.setId(rs.getLong("id"));
        branchInfo.setTid(rs.getLong("tid"));
        branchInfo.setBid(rs.getLong("branch_id"));
        branchInfo.setParentId(rs.getString("parent_id"));
        branchInfo.setStatus(rs.getInt("status"));
        branchInfo.setTransactionName(rs.getString("transaction_name"));
        branchInfo.setTimeout(rs.getLong("timeout"));
        branchInfo.setTimeoutType(rs.getInt("timeout_type"));
        branchInfo.setRetryCount(rs.getInt("retry_count"));
        branchInfo.setTransType(rs.getInt("trans_type"));
        branchInfo.setParams(rs.getBytes("params"));
        branchInfo.setResourceId(rs.getString("resource_id"));
        branchInfo.setCreateTime(rs.getLong("create_time"));
        branchInfo.setModifyTime(rs.getLong("modify_time"));
        branchInfo.setEndTime(rs.getLong("end_time"));
        return branchInfo;
    }

    @Override
    public int updateStatus(Long tid, Long branchId, Integer fromStatus, Integer toStatus) throws SQLException {
        String sql = "UPDATE " + TRANS_BRANCH + " SET status =?,modify_time=?  where tid =? and branch_id=? and status=?";
        int paramIndex = 1;
        try {
            ScDataSource dataSource = ScDataSourceRecorder.getDefaultDataSource();
            try (Connection connection = dataSource.getOriginalConnection()) {
                try (PreparedStatement pst = connection.prepareStatement(sql)) {
                    pst.setInt(paramIndex++, toStatus);
                    pst.setLong(paramIndex++, System.currentTimeMillis());
                    pst.setLong(paramIndex++, tid);
                    pst.setLong(paramIndex++, branchId);
                    pst.setInt(paramIndex, fromStatus);
                    return pst.executeUpdate();
                }
            }
        }catch (Exception e){
            throw new SQLException(e);
        }
    }

    @Override
    public void delete(Long tid, Long branchId) throws SQLException {
        int paramIndex = 1;
        String sql = "DELETE FROM " + TRANS_BRANCH + " WHERE tid=? and branch_id=?";
        try {
            ScDataSource dataSource = ScDataSourceRecorder.getDefaultDataSource();
            try (Connection connection = dataSource.getOriginalConnection()) {
                try (PreparedStatement pst = connection.prepareStatement(sql)) {
                    pst.setLong(paramIndex++, tid);
                    pst.setLong(paramIndex, branchId);
                    pst.executeUpdate();
                }
            }
        }catch (Exception e){
            throw new SQLException(e);
        }
    }

    @Override
    public int updateRetryCount(long id, int retryCount) throws SQLException {
        String sql = "UPDATE " + TRANS_BRANCH + " SET retry_count =? ,modify_time=? where id =? ";
        int paramIndex = 1;
        try {
            ScDataSource dataSource = ScDataSourceRecorder.getDefaultDataSource();
            try (Connection connection = dataSource.getOriginalConnection()) {
                try (PreparedStatement pst = connection.prepareStatement(sql)) {
                    pst.setInt(paramIndex++, retryCount);
                    pst.setLong(paramIndex++, System.currentTimeMillis());
                    pst.setLong(paramIndex, id);
                    return pst.executeUpdate();
                }
            }
        }catch (Exception e){
            throw new SQLException(e);
        }
    }

    @Override
    public int updateModifyTime(long id, long modifyTime) throws SQLException {
        String sql = "UPDATE " + TRANS_BRANCH + " SET modify_time=? where id =? ";
        int paramIndex = 1;
        try {
            ScDataSource dataSource = ScDataSourceRecorder.getDefaultDataSource();
            try (Connection connection = dataSource.getOriginalConnection()) {
                try (PreparedStatement pst = connection.prepareStatement(sql)) {
                    pst.setLong(paramIndex++, modifyTime);
                    pst.setLong(paramIndex, id);
                    return pst.executeUpdate();
                }
            }
        }catch (Exception e){
            throw new SQLException(e);
        }
    }

    @Override
    public int updateStatusAndEndTimeById(long id, Integer status, long endTime, long modifyTime) throws SQLException {
        String sql = "UPDATE " + TRANS_BRANCH + " SET status=?,end_time=?, modify_time=? where id =? ";
        int paramIndex = 1;
        try {
            ScDataSource dataSource = ScDataSourceRecorder.getDefaultDataSource();
            try (Connection connection = dataSource.getOriginalConnection()) {
                try (PreparedStatement pst = connection.prepareStatement(sql)) {
                    pst.setObject(paramIndex++, status);
                    pst.setLong(paramIndex++, endTime);
                    pst.setLong(paramIndex++, modifyTime);
                    pst.setLong(paramIndex, id);
                    return pst.executeUpdate();
                }
            }
        }catch (Exception e){
            throw new SQLException(e);
        }
    }
}
