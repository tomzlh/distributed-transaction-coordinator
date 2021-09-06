
package com.ops.sc.core.rest.handler;

/**
 * If an exception was thrown
 * will search a proper handler to handle it.
 *
 * @param <E> Type of Exception
 */
public interface ExceptionHandler<E extends Throwable> {
    
    /**
     * Handler for specific Exception.
     *
     * @param ex Exception
     * @return Handle result
     */
    ExceptionHandleResult handleException(E ex);
}
