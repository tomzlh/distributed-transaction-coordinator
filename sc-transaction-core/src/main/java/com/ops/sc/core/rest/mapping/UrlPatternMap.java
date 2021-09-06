
package com.ops.sc.core.rest.mapping;


public interface UrlPatternMap<V> {
    
    /**
     * Add a path pattern and value to URL pattern map.
     *
     * @param pathPattern path pattern
     * @param value payload of the path pattern
     */
    void put(String pathPattern, V value);
    
    /**
     * Find a proper mapping context for current path.
     *
     * @param path a path to match.
     * @return a mapping context if the path matched a pattern, return null if mismatched.
     */
    MappingContext<V> match(String path);
}
