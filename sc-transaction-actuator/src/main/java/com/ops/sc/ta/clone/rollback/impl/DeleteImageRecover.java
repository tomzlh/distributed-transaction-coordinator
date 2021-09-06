package com.ops.sc.ta.clone.rollback.impl;

import com.ops.sc.core.clone.RollbackItem;
import com.ops.sc.core.clone.RollbackLine;
import com.ops.sc.core.clone.RollbackLineField;
import com.ops.sc.ta.clone.enums.SqlType;
import com.ops.sc.ta.clone.rollback.ImageRecover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Component("deleteImageRecover")
public class DeleteImageRecover implements ImageRecover {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteImageRecover.class);

    @Override
    public SqlType getSqlType() {
        return SqlType.DELETE;
    }

    @Override
    public boolean checkAfterImage(RollbackItem item, Connection connection) {
        String sql = "select count(1) from " + item.getTableName() + item.getSqlIdWhere();
        try (Statement sm = connection.createStatement(); ResultSet rs = sm.executeQuery(sql)) {
            if (!rs.next()) {
                return false;
            } else {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void rollbackBeforeImage(RollbackItem item, Connection connection) throws SQLException {
        final List<RollbackLine> lines = item.getBeforeImage().getLines();
        if (CollectionUtils.isEmpty(lines)) {
            return;
        }

        List<String> columnList = new ArrayList<>();
        List<String> placeList = new ArrayList<>();
        for (RollbackLineField field : lines.get(0).getFields()) {
            columnList.add(field.getName());
            placeList.add("?");
        }

        String column = StringUtils.collectionToDelimitedString(columnList, ",");
        String placeholder = StringUtils.collectionToDelimitedString(placeList, ",");

        String sql = "insert into " + item.getTableName() + " (" + column + ") values(" + placeholder + ")";
        LOGGER.debug("Rollback deleteImage. sql: {}", sql);
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            for (int i = 0; i < lines.size(); i++) {
                List<RollbackLineField> fields = lines.get(i).getFields();
                int index = 1;
                for (RollbackLineField field : fields) {
                    if (field.getType() != null && field.getType().toLowerCase().contains("timestamp")) {
                        pst.setTimestamp(index++, Timestamp.valueOf(field.getValue().toString()));
                    } else {
                        pst.setObject(index++, field.getValue());
                    }
                }
                pst.executeUpdate();
            }
        }
    }
}