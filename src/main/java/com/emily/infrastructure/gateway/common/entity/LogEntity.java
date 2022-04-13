package com.emily.infrastructure.gateway.common.entity;

import com.emily.infrastructure.gateway.common.HttpUtils;
import com.emily.infrastructure.gateway.common.enums.TraceType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.io.Serializable;
import java.util.UUID;

/**
 * @program: EmilyGateway
 * @description: 日志实体类
 * @create: 2021/01/15
 */
public class LogEntity implements Serializable {
    /**
     * 唯一标识
     */
    private String traceId = UUID.randomUUID().toString();
    /**
     * 日志类型
     */
    private TraceType traceType = TraceType.INFO;
    /**
     * 当前请求的基础连接ID，与网关有关
     */
    private String cId;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 请求类型
     */
    private String contentType;
    /**
     * 协议
     */
    private String schema;
    /**
     * 请求URL
     */
    private String url;
    /**
     * 请求header
     */
    private HttpHeaders headers;
    /**
     * 请求参数
     */
    private Object requestBody;
    /**
     * 响应数据
     */
    private Object responseBody;
    /**
     * 请求时间
     */
    private long time;

    public LogEntity() {
    }

    public LogEntity(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        this.setcId(request.getId());
        this.setMethod(HttpUtils.getMethod(request));
        this.setRequestBody(HttpUtils.getRequestBody(exchange));
        this.setContentType(HttpUtils.getContentType(request));
        this.setSchema(HttpUtils.getSchema(request));
        this.setHeaders(request.getHeaders());
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
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

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public TraceType getTraceType() {
        return traceType;
    }

    public void setTraceType(TraceType traceType) {
        this.traceType = traceType;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }
}
