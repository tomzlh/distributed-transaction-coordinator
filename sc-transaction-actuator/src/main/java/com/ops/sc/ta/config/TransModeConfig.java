package com.ops.sc.ta.config;

import lombok.Data;


@Data
public class TransModeConfig {

      private String modeName;

      private String prepareMethod;

      private String commitMethod;

      private String rollbackMethod;

}
