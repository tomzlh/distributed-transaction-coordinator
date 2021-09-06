
package com.ops.sc.core.rest;

/**
 * A facade of restful service. Invoke startup() method to start listen a port to provide Restful API.
 */
public interface NettyFacade {
    
    /**
     * Start RESTFul service.
     */
    void startup();
    
    /**
     * Shutdown RESTFul service.
     */
    void shutdown();
}
