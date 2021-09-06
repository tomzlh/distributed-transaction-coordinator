package com.ops.sc.server.interceptor;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ops.sc.common.constant.ServerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.hold.ResponseStatus;
import com.ops.sc.common.hold.RequestProxy;
import com.ops.sc.common.utils.JsonUtil;


public class AuthInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInterceptor.class);

    private static final List<String> AUTH_LIST = Lists.newArrayList(ServerConstants.HttpAction.CREATE_TRANS_GROUP, ServerConstants.HttpAction.DELETE_TRANS_GROUP);

    private String authUrl;

    public AuthInterceptor(String authUrl) {
        this.authUrl = authUrl;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String operation = request.getParameter(ServerConstants.HttpConst.PARAM_ACTION);
        String authJWT = request.getHeader(ServerConstants.HttpConst.HEADER_AUTH_JWT);
        String accountId = request.getHeader(ServerConstants.HttpConst.HEADER_ACCOUNT_ID);

        if (AUTH_LIST.contains(operation) && !Strings.isNullOrEmpty(accountId)) {
            LOGGER.debug("Start auth for  authJWT : {}, operation : {} and status : {}",
                    authJWT,
                    operation);
            Map<String, String> queryParam = getParamMap(operation, accountId);
            ResponseStatus result = RequestProxy.get(authUrl, queryParam, null);
            boolean hasRole = result.isSuccess()
                    && (Boolean) JsonUtil.toMap(result.getBody()).get(ServerConstants.AuthConst.HAS_ROLE);
            if (!hasRole) {
                LOGGER.warn("Auth fail accountId : {},authJWT : {}, operation : {} and status : {}",
                        accountId, authJWT, operation, result.getStatusCode());
                response.setStatus(ServerConstants.HttpConst.PERMISSION_FORBIDDEN);
                // 构建返回值
                ResponseResult responseResult = ResponseResult.returnResult(TransactionResponseCode.AUTH_FAILED);
                response.getWriter().append(JsonUtil.toString(responseResult));
                return false;
            }
        }
        return super.preHandle(request, response, handler);
    }

    private Map<String, String> getParamMap(String operation, String accountId) {
        Map<String, String> queryParam = Maps.newHashMap();
        queryParam.put("ServiceModule", ServerConstants.SERVICE_NAME);
        queryParam.put("ResourceType", ServerConstants.AuthConst.RESOURCE_TYPE);
        queryParam.put("OperationType", operation);
        // queryParam.put("JWT", authJWT);
        queryParam.put("AccountId", accountId);
        queryParam.put("Action", "Authentication");
        queryParam.put("Version", "2021-09-09");
        return queryParam;
    }
}
