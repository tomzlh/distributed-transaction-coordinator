
package com.ops.sc.core.rest.handler;

import com.ops.sc.core.rest.Http;
import com.ops.sc.core.rest.annotation.Parameter;
import com.ops.sc.core.rest.annotation.RequestBody;
import com.ops.sc.core.rest.enums.ParamPart;
import com.ops.sc.core.rest.annotation.ReturnT;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Handle holds a handle method and an instance for method invoking.
 * Describes parameters requirements of handle method.
 */
public final class Handler {
    
    private final Object instance;
    
    private final Method handleMethod;
    
    @Getter
    private final List<HandlerParameter> handlerParameters;
    
    /**
     * HTTP status code to return.
     */
    @Getter
    private final int httpStatusCode;
    
    /**
     * Content type to producing.
     */
    @Getter
    private final String producing;
    
    public Handler(final Object instance, final Method handleMethod) {
        this.instance = instance;
        this.handleMethod = handleMethod;
        this.handlerParameters = parseHandleMethodParameter();
        this.httpStatusCode = parseReturning();
        this.producing = parseProducing();
    }
    
    /**
     * Execute handle method with required arguments.
     *
     * @param args Required arguments
     * @return Method invoke result
     * @throws InvocationTargetException Wraps exception thrown by invoked method
     * @throws IllegalAccessException    Handle method is not accessible
     */
    public Object execute(final Object... args) throws InvocationTargetException, IllegalAccessException {
        return handleMethod.invoke(instance, args);
    }
    
    private List<HandlerParameter> parseHandleMethodParameter() {
        List<HandlerParameter> params = new LinkedList<>();
        java.lang.reflect.Parameter[] parameters = handleMethod.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            java.lang.reflect.Parameter parameter = parameters[i];
            Parameter annotation = parameter.getAnnotation(Parameter.class);
            HandlerParameter handlerParameter;
            RequestBody requestBody;
            if (null != annotation) {
                handlerParameter = new HandlerParameter(i, parameter.getType(), annotation.source(), annotation.name(), annotation.required());
            } else if (null != (requestBody = parameter.getAnnotation(RequestBody.class))) {
                handlerParameter = new HandlerParameter(i, parameter.getType(), ParamPart.BODY, parameter.getName(), requestBody.required());
            } else {
                handlerParameter = new HandlerParameter(i, parameter.getType(), ParamPart.UNKNOWN, parameter.getName(), false);
            }
            params.add(handlerParameter);
        }
        return Collections.unmodifiableList(params);
    }
    
    private int parseReturning() {
        ReturnT returnT = handleMethod.getAnnotation(ReturnT.class);
        return Optional.ofNullable(returnT).map(ReturnT::code).orElse(200);
    }
    
    private String parseProducing() {
        ReturnT returnT = handleMethod.getAnnotation(ReturnT.class);
        return Optional.ofNullable(returnT).map(ReturnT::contentType).orElse(Http.DEFAULT_CONTENT_TYPE);
    }
}
