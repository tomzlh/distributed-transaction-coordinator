
package com.ops.sc.common.spi;

import java.util.Properties;


public interface SPIPostProcessor {
    
    /**
     * Initialize SPI instance.
     * 
     * @param props properties
     */
    void init(Properties props);
}
