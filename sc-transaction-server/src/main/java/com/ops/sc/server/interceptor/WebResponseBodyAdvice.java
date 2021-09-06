package com.ops.sc.server.interceptor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.ops.sc.core.service.ResourceInfoService;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.constant.ServerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


@ControllerAdvice
public class WebResponseBodyAdvice implements ResponseBodyAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebResponseBodyAdvice.class);

    private static final String CHINESE_HEADER = "zh";
    private static final String ENGLISH_HEADER = "en";
    private static final Locale DEFAULT_LANGUAGE_LOCALE = Locale.ENGLISH;

    private static final Map<String, Locale> HEADER_TO_LOCALE_MAP = new HashMap<>();

    @Resource
    private ResourceInfoService resourceInfoService;


    @Value("${skip.deploy:true}")
    private Boolean skipDeploy;


    @PostConstruct
    public void initMap() {
        HEADER_TO_LOCALE_MAP.put(CHINESE_HEADER.toUpperCase(), Locale.CHINESE);
        HEADER_TO_LOCALE_MAP.put(ENGLISH_HEADER.toUpperCase(), Locale.ENGLISH);
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return ResponseResult.class.isAssignableFrom(returnType.getMethod().getReturnType());
    }

    @Override
    public Object beforeBodyWrite(Object obj, MethodParameter methodParameter, MediaType mediaType, Class aClass,
            ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        ResponseResult body = (ResponseResult) obj;
        Locale locale = null;
        if (serverHttpRequest.getHeaders().containsKey(ServerConstants.HttpConst.HEADER_LANGUAGE)) {
            String language = serverHttpRequest.getHeaders().getFirst(ServerConstants.HttpConst.HEADER_LANGUAGE);
            locale = HEADER_TO_LOCALE_MAP.get(language.toUpperCase());
        }
        try {
            // 默认为message_zh.properties
            String message = resourceInfoService.getMessage(body.getError().getDescKey(), locale,
                    DEFAULT_LANGUAGE_LOCALE, body.getErrorArgs());
            body.setMessage(message);
        } catch (NoSuchMessageException e) {
            // error对应的信息没有resourceBundle中
            LOGGER.error("messageSource get message catch an NoSuchMessageException: ", e);
        }
        body.setCode(body.getError().getCode());
        serverHttpResponse.setStatusCode(HttpStatus.valueOf(body.getError().getHttpCode()));


        return body;
    }


    public  String getSourceIpAddress(HttpServletRequest httpServletRequest) {
        if (httpServletRequest != null) {
            String remoteAddr = httpServletRequest.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = httpServletRequest.getRemoteAddr();
                return remoteAddr;
            } else {
                remoteAddr = remoteAddr.replace(" ", "");
                String[] ips = remoteAddr.split(",");

                return ips[0];
            }
        }
        return null;
    }

}
