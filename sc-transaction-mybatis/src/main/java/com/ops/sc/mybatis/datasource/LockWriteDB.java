package com.ops.sc.mybatis.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "sc.db.lock.write")
@Component(value = "lockWriteDB")
public class LockWriteDB extends DatabaseInfo {

}
