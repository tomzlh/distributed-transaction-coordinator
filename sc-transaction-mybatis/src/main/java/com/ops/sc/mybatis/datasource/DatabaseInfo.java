package com.ops.sc.mybatis.datasource;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ToString
public class DatabaseInfo {

    private String driverClassName;

    private String url;

    private String password;

    private String username;

    private int initialSize;

    private int maxTotal;

    private int minIdle;

    private int maxIdle;

    private long minEvictIdleTimeMillis;

    private long timeEvictRunMillis;

    private boolean testOnBorrow;

    private boolean testWhileIdle;

    private String validationQuery;

    private int numTestsPerEvictRun;

}
