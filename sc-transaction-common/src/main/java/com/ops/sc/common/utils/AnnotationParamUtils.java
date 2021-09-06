package com.ops.sc.common.utils;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AnnotationParamUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationParamUtils.class);

    public static String getIdFromParam(ProceedingJoinPoint pjp, String param) {
        if (Strings.isNullOrEmpty(param)) {
            throw  new RuntimeException("param is null or empty!");
        }
        try {
                Method method = ((MethodSignature) pjp.getSignature()).getMethod();
                Object[] args = pjp.getArgs();
                if (args.length == 0) {
                    return StringUtils.EMPTY;
                }
                Parameter[] parameters = method.getParameters();
                LocalVariableTableParameterNameDiscoverer lvtPnd = new LocalVariableTableParameterNameDiscoverer();
                String[] parameterNames = lvtPnd.getParameterNames(method);
                for (int i = 0; i < parameters.length; i++) {
                    Class parameterType = parameters[i].getType();
                    if (parameterType.getClassLoader() == null) {
                        if (isCollectionClassType(parameterType)) {
                            LOGGER.warn("Not supported collection class type : " + parameterType + " for param");
                            continue;
                        }
                        if (parameterNames[i].equals(param)) {
                            return String.valueOf(args[i]);
                        }
                    } else {
                        Field field;
                        try {
                            field = parameterType.getDeclaredField(param);
                        } catch (NoSuchFieldException e) {
                            LOGGER.warn("No such declared field:" + param, e);
                            continue;
                        }
                        field.setAccessible(true);
                        return String.valueOf(field.get(args[i]));
                    }
                }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return StringUtils.EMPTY;
    }

    public static String getResourceId(String appName, String methodName) {
        return appName + "." + methodName;
    }

    private static boolean isCollectionClassType(Class clazz) {
        if (clazz.isAssignableFrom(List.class) || clazz.isAssignableFrom(Set.class) || clazz.isAssignableFrom(Map.class)
                || clazz.isAssignableFrom(Queue.class)) {
            return true;
        }
        return false;
    }
}
