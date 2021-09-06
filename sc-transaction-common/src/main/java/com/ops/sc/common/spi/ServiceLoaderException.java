
package com.ops.sc.common.spi;

/**
 * Service loader exception.
 */
public final class ServiceLoaderException extends RuntimeException {
    
    private static final long serialVersionUID = -3949913598320994076L;
    
    public ServiceLoaderException(final Class<?> clazz, final Throwable cause) {
        super(String.format("Not find public no args constructor for SPI class `%s`", clazz.getName()), cause);
    }

    public ServiceLoaderException(final String desc, final String cause) {
        super(String.format("desc "+"`%s`", cause));
    }
}
