package com.emily.cloud.gateway.entity;

import com.emily.cloud.gateway.utils.HttpUtils;
import com.emily.cloud.gateway.utils.enums.TraceType;
import com.emily.framework.common.enums.DateFormatEnum;
import com.emily.framework.common.utils.json.JSONUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
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
    private String schema;
    /**
     * 请求URL
     */
    private String url;
    /**
     * 请求参数
     */
    private Object requestBody;
    /**
     * 请求header
     */
    private HttpHeaders headers;
    /**
     * 响应数据
     */
    private Object data;
    /**
     * 请求时间
     */
    private String startDate;
    /**
     * 响应时间
     */
    private String endDate;

    public LogEntity() {
    }

    public LogEntity(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        this.setcId(request.getId());
        this.setMethod(request.getMethodValue());
        DataBuffer dataBuffer = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
        try {
            this.setRequestBody(dataBuffer == null ? null : JSONUtils.toJavaBean(dataBuffer.toString(StandardCharsets.UTF_8), Map.class));
        } catch (Exception e) {
            this.setRequestBody(dataBuffer == null ? null : dataBuffer.toString(StandardCharsets.UTF_8));
        }
        this.setStartDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormatEnum.YYYY_MM_DD_HH_MM_SS_SSS.getFormat())));
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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
