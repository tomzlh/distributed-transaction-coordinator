package com.ops.sc.ta.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "sc.ta.trans")
public class TransModeConfigList {

      private List<TransModeConfig> modes=new ArrayList<>();
}
