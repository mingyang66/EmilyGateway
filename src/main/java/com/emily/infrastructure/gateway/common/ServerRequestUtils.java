package com.emily.infrastructure.gateway.common;

import com.emily.infrastructure.gateway.common.enums.HttpStatusType;
import com.emily.infrastructure.json.JsonUtils;
import com.google.common.collect.Maps;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * @program: EmilyGateway
 * @description: 请求工具类
 * @author:
 * @create: 2021/03/04
 */
public class ServerRequestUtils {
    /**
     * 获取网关请求IP
     */
    public static String getIp(ServerHttpRequest request) {
        Assert.notNull(request, HttpStatusType.ILLEGAL_ARGUMENT.getMessage());
        return request.getRemoteAddress().getHostString();
    }

    /**
     * 获取请求路径
     */
    public static String getUrl(ServerWebExchange exchange) {
        Assert.notNull(exchange, HttpStatusType.ILLEGAL_ARGUMENT.getMessage());
        return exchange.getAttributes().containsKey(GATEWAY_REQUEST_URL_ATTR) ? exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR).toString() : exchange.getRequest().getURI().toString();
    }

    /**
     * 网关请求协议
     */
    public static String getSchema(ServerHttpRequest request) {
        Assert.notNull(request, HttpStatusType.ILLEGAL_ARGUMENT.getMessage());
        return request.getURI().getScheme();
    }

    /**
     * 获取请求Method
     */
    public static String getMethod(ServerHttpRequest request) {
        Assert.notNull(request, HttpStatusType.ILLEGAL_ARGUMENT.getMessage());
        return request.getMethodValue();
    }

    /**
     * 获取请求ContentType
     *
     * @param request
     * @return
     */
    public static String getContentType(ServerHttpRequest request) {
        Assert.notNull(request, HttpStatusType.ILLEGAL_ARGUMENT.getMessage());
        return request.getHeaders().getContentType() == null ? null : MediaType.toString(Arrays.asList(request.getHeaders().getContentType()));
    }

    /**
     * 获取请求参数
     */
    public static Object getRequestParams(ServerWebExchange exchange) {
        Assert.notNull(exchange, HttpStatusType.ILLEGAL_ARGUMENT.getMessage());
        Map<String, Object> paramsMap = Maps.newHashMap();
        paramsMap.put("headers", exchange.getRequest().getHeaders());
        DataBuffer dataBuffer = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
        try {
            if (dataBuffer == null) {
                return paramsMap;
            }
            paramsMap.putAll(JsonUtils.toJavaBean(dataBuffer.toString(StandardCharsets.UTF_8), Map.class));
        } catch (Exception e) {
            paramsMap.put("result", dataBuffer == null ? null : dataBuffer.toString(StandardCharsets.UTF_8));
        }
        return paramsMap;
    }
}
