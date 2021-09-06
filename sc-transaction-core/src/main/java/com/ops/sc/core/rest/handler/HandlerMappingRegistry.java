
package com.ops.sc.core.rest.handler;

import com.ops.sc.core.rest.mapping.RegexUrlPatternMap;
import com.ops.sc.core.rest.mapping.UrlPatternMap;
import com.ops.sc.core.rest.mapping.MappingContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public final class HandlerMappingRegistry {
    
    private final Map<HttpMethod, UrlPatternMap<Handler>> mappings = new HashMap<>();


    public static HandlerMappingRegistry getInstance() {
        return HandlerMappingRegistry.InstanceBuilder.instance;
    }
    
    /**
     * Get a MappingContext with Handler for the request.
     *
     * @param httpRequest HTTP request
     * @return A MappingContext if matched, return null if mismatched.
     */
    public MappingContext<Handler> getMappingContext(final HttpRequest httpRequest) {
        UrlPatternMap<Handler> urlPatternMap = mappings.get(httpRequest.method());
        String uriWithoutQuery = httpRequest.uri().split("\\?")[0];
        return Optional
                .ofNullable(urlPatternMap.match(uriWithoutQuery))
                .orElse(null);
    }
    
    /**
     * Add a Handler for a path pattern.
     *
     * @param method HTTP method
     * @param pathPattern path pattern
     * @param handler handler
     */
    public void addMapping(final HttpMethod method, final String pathPattern, final Handler handler) {
        mappings.computeIfAbsent(method, httpMethod -> new RegexUrlPatternMap<>());
        UrlPatternMap<Handler> urlPatternMap = mappings.get(method);
        urlPatternMap.put(pathPattern, handler);
    }

    private static class InstanceBuilder {
        static HandlerMappingRegistry instance = new HandlerMappingRegistry();
    }
}
