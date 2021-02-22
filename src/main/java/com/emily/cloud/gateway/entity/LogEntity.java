package com.emily.cloud.gateway.entity;

import com.emily.cloud.gateway.utils.JSONUtils;
import com.emily.cloud.gateway.utils.enums.DateFormatEnum;
import com.emily.cloud.gateway.utils.enums.TraceType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;

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
    private String protocol;
    /**
     * 请求URL
     */
    private String url;
    /**
     * 请求参数
     */
    private Object params;
    /**
     * 响应数据
     */
    private Object data;
    /**
     * 响应数据类型
     */
    private String responseContentType;
    /**
     * 请求时间
     */
    private String requestDate;
    /**
     * 响应时间
     */
    private String responseDate;

    public LogEntity() {
    }

    public LogEntity(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        this.setcId(request.getId());
        this.setMethod(request.getMethodValue());
        DataBuffer dataBuffer = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
        if (request.getHeaders().getContentType() != null && request.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
            this.setParams(dataBuffer == null ? null : JSONUtils.toJavaBean(dataBuffer.toString(StandardCharsets.UTF_8), Map.class));
        } else {
            this.setParams(dataBuffer == null ? null : dataBuffer.toString(StandardCharsets.UTF_8));
        }
        this.setRequestDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormatEnum.YYYY_MM_DD_HH_MM_SS_SSS.getFormat())));
        this.setContentType(request.getHeaders().getContentType() == null ? null : MediaType.toString(Arrays.asList(request.getHeaders().getContentType())));
        this.setProtocol(request.getURI().getScheme());
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

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(String responseDate) {
        this.responseDate = responseDate;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    public TraceType getTraceType() {
        return traceType;
    }

    public void setTraceType(TraceType traceType) {
        this.traceType = traceType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
