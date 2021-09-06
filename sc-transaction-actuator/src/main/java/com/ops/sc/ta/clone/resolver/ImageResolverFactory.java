package com.ops.sc.ta.clone.resolver;


import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.ta.clone.dto.SqlInfo;
import com.ops.sc.ta.clone.enums.SqlType;
import com.ops.sc.ta.clone.resolver.impl.DeleteImageParser;
import com.ops.sc.ta.clone.resolver.impl.InsertImageParser;
import com.ops.sc.ta.clone.resolver.impl.SelectImageParser;
import com.ops.sc.ta.clone.resolver.impl.UpdateImageParser;

public class ImageResolverFactory {

    public static ImageParser findResolver(SqlInfo sqlInfo, String sql) throws ScClientException{

        if (SqlType.INSERT.name().equals(sqlInfo.getSqlType())) {
            return new InsertImageParser(sqlInfo);
        }
        if (SqlType.UPDATE.name().equals(sqlInfo.getSqlType())) {
            return new UpdateImageParser(sqlInfo);
        }
        if (SqlType.DELETE.name().equals(sqlInfo.getSqlType())) {
            return new DeleteImageParser(sqlInfo);
        }
        if (SqlType.SELECT.name().equals(sqlInfo.getSqlType())) {
            return new SelectImageParser(sqlInfo);
        }
        throw new ScClientException(ClientErrorCode.UNSUPPORTED_SQL, "不支持SQL类型 sql=" + sql);
    }

}
