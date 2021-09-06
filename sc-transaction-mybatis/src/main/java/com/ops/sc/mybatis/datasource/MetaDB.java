package com.ops.sc.mybatis.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "sc.db.meta")
@Component(value = "metaDB")
public class MetaDB extends DatabaseInfo {

}
