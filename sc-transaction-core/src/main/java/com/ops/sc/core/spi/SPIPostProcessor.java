package com.ops.sc.core.spi;

import java.util.Map;
import java.util.Properties;

public interface SPIPostProcessor {

    void init(Map<String,String> map);
}
