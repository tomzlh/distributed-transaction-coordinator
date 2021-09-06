package com.ops.sc.admin.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sc.trans.compensator")
@Data
public class AppConfiguration {

    private String appName;

    private String groupId;

    private String serverCluster;
}
