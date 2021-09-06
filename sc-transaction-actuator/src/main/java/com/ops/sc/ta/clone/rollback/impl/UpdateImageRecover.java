package com.ops.sc.ta.clone.rollback.impl;

import com.ops.sc.core.clone.RollbackItem;
import com.ops.sc.core.clone.RollbackLine;
import com.ops.sc.core.clone.RollbackLineField;
import com.ops.sc.ta.clone.enums.SqlType;
import com.ops.sc.ta.clone.rollback.BaseRecover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component("updateImageRecover")
public class UpdateImageRecover extends BaseRecover {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateImageRecover.class);

    @Override
    public SqlType getSqlType() {
        return SqlType.UPDATE;
    }

    @Override
    public void rollbackBeforeImage(RollbackItem item, Connection connection) throws SQLException {
        final List<RollbackLine> lines = item.getBeforeImage().getLines();
        if (CollectionUtils.isEmpty(lines)) {
            return;
        }

        List<String> columnPlaceList = new ArrayList<>();
        for (RollbackLineField field : lines.get(0).getFields()) {
            if (!item.getPrimaryKey().equals(field.getName())) {
                columnPlaceList.add(field.getName() + "=?");
            }
        }

        String column = StringUtils.collectionToDelimitedString(columnPlaceList, ",");
        String sql = String.format("update %s set %s where %s=?", item.getTableName(), column, item.getPrimaryKey());
        LOGGER.debug("Rollback updateImage. sql: {}", sql);
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            for (int i = 0; i < lines.size(); i++) {
                List<RollbackLineField> fields = lines.get(i).getFields();
                int index = 1;
                for (RollbackLineField field : fields) {
                    if (item.getPrimaryKey().equals(field.getName())) {
                        pst.setObject(fields.size(), field.getValue());
                    } else if (field.getType() != null && field.getType().toLowerCase().contains("timestamp")) {
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
