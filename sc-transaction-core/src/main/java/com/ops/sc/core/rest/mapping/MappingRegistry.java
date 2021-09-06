package com.ops.sc.core.rest.mapping;

import com.ops.sc.core.rest.annotation.MapPath;
import com.ops.sc.core.rest.annotation.RootContext;
import com.ops.sc.core.rest.config.RpcServiceConfiguration;
import com.ops.sc.core.rest.handler.Handler;
import com.ops.sc.core.rest.handler.HandlerMappingRegistry;
import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class MappingRegistry {

    private static final String TRAILING_SLASH = "/";

    public static void initMappingRegistry(final List<Object> restfulControllers) {
        for (Object restfulController : restfulControllers) {
            Class<?> controllerClass = restfulController.getClass();
            String contextPath = Optional.ofNullable(controllerClass.getAnnotation(RootContext.class)).map(RootContext::value).orElse("");
            for (Method method : controllerClass.getMethods()) {
                MapPath mapPath = method.getAnnotation(MapPath.class);
                if (null == mapPath) {
                    continue;
                }
                HttpMethod httpMethod = HttpMethod.valueOf(mapPath.method());
                String path = mapPath.path();
                String fullPathPattern = resolveFullPath(contextPath, path);
                if (!RpcServiceConfiguration.isTrailingSlashSensitive()) {
                    fullPathPattern = appendTrailingSlashIfAbsent(fullPathPattern);
                }
                HandlerMappingRegistry.getInstance().addMapping(httpMethod, fullPathPattern, new Handler(restfulController, method));
            }
        }
    }

    private static String resolveFullPath(final String contextPath, final String pattern) {
        return Optional.ofNullable(contextPath).orElse("") + pattern;
    }

    private static String appendTrailingSlashIfAbsent(final String uri) {
        String[] split = uri.split("\\?");
        if (1 == split.length) {
            return uri.endsWith(TRAILING_SLASH) ? uri : uri + TRAILING_SLASH;
        }
        String path = split[0];
        return path.endsWith(TRAILING_SLASH) ? uri : path + TRAILING_SLASH + "?" + split[1];
    }
}
