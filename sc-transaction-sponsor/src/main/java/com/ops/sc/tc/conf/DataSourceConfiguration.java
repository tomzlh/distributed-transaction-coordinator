package com.ops.sc.tc.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sc.db")
public class DataSourceConfiguration {

     private String dataSourceName;

     private String xaDataSourceName;
}
