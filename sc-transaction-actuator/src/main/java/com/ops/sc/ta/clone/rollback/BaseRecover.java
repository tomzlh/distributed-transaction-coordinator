package com.ops.sc.ta.clone.rollback;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.core.clone.RollbackItem;
import com.ops.sc.core.clone.RollbackLine;
import com.ops.sc.core.clone.RollbackLineField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;


public abstract class BaseRecover implements ImageRecover {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseRecover.class);


    @Override
    public boolean checkAfterImage(RollbackItem item, Connection connection) {
        String primaryKey = item.getPrimaryKey();
        List<RowInfoMapWrapper> imageRowList = new ArrayList<>();
        for (RollbackLine line : item.getAfterImage().getLines()) {
            Map<String, Object> imageRow = new LinkedHashMap<>(line.getFields().size());
            String pkValue = null;
            for (RollbackLineField field : line.getFields()) {
                imageRow.put(field.getName(), field.getValue());
                if (field.getName().equalsIgnoreCase(primaryKey)) {
                    pkValue = String.valueOf(field.getValue());
                }
            }
            imageRowList.add(new RowInfoMapWrapper(pkValue, imageRow));
        }

        List<RowInfoMapWrapper> currentDbRowList;
        String sql = item.getSqlSelect() + item.getSqlIdWhere();
        try (Statement sm = connection.createStatement(); ResultSet rs = sm.executeQuery(sql)) {
            currentDbRowList = resultSetToRowInfoWrapper(rs, primaryKey);
        } catch (SQLException e) {
            return false;
        }

        if (imageRowList.size() != currentDbRowList.size()) {
            LOGGER.warn("Current db data size {} is not equal to image log size {}", imageRowList.size(),
                    currentDbRowList.size());
            return false;
        }

        Collections.sort(imageRowList);
        Collections.sort(currentDbRowList);

        String imageJson = JsonUtil.toString(imageRowList, SerializerFeature.WriteDateUseDateFormat);
        String dbJson = JsonUtil.toString(currentDbRowList, SerializerFeature.WriteDateUseDateFormat);

        if (!imageJson.equals(dbJson)) {
            LOGGER.warn("ImageJson: {} dbJson: {} not equals", imageJson, dbJson);
            return false;
        }
        return true;
    }

    private List<RowInfoMapWrapper> resultSetToRowInfoWrapper(ResultSet rs, String primaryKey) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<RowInfoMapWrapper> list = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>(columns);
            String pkValue = null;
            for (int i = 1; i <= columns; ++i) {
                row.put(md.getColumnName(i), rs.getObject(i));
                if (md.getColumnName(i).equalsIgnoreCase(primaryKey)) {
                    pkValue = String.valueOf(rs.getObject(i));
                }
            }
            list.add(new RowInfoMapWrapper(pkValue, row));
        }
        return list;
    }

}
