package com.emily.infrastructure.gateway.common.entity;

import com.emily.infrastructure.gateway.common.ServerRequestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Emily
 * @program: EmilyGateway
 * @description: 日志实体类
 * @create: 2021/01/15
 */
public class BaseLogger implements Serializable {
    /**
     * 唯一标识
     */
    private String traceId = UUID.randomUUID().toString();
    /**
     * 请求方法
     */
    private String method;
    /**
     * 请求URL
     */
    private String url;
    /**
     * 请求参数
     */
    private Object requestParams;
    /**
     * 响应数据
     */
    private Object responseBody;
    /**
     * 请求时间
     */
    private long time;

    public BaseLogger() {
    }

    public BaseLogger(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        this.method = ServerRequestUtils.getMethod(request);
        this.requestParams = ServerRequestUtils.getRequestParams(exchange);
        this.url = ServerRequestUtils.getUrl(exchange);
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(Object requestParams) {
        this.requestParams = requestParams;
    }

    public Object getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
