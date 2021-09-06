package com.ops.sc.tc.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sc.trans.base")
@Data
public class TransInfoConfiguration {

    private String appName;

    private String groupId;

    private String serverCluster;

    private boolean isServiceDisable;

}
