package com.emily.cloud.gateway.entity;

import com.emily.cloud.gateway.utils.enums.DateFormatEnum;
import com.emily.cloud.gateway.utils.enums.TraceType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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

    public LogEntity(){}

    public LogEntity(ServerWebExchange exchange){
        ServerHttpRequest request = exchange.getRequest();
        this.setcId(request.getId());
        this.setMethod(request.getMethodValue());
        this.setParams(getRequestParams(exchange));
        this.setRequestDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormatEnum.YYYY_MM_DD_HH_MM_SS_SSS.getFormat())));
        MediaType mediaType = request.getHeaders().getContentType();
        if(mediaType != null){
            this.setContentType(MediaType.toString(Arrays.asList(mediaType)));
        }
    }
    /**
     * 获取请求参数
     * @param exchange 网关上下文
     */
    protected Object getRequestParams(ServerWebExchange exchange){
        String bodyString = null;
        ServerHttpRequest request = exchange.getRequest();
        if (request.getMethod() == HttpMethod.POST) {
            DataBuffer body = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
            bodyString = com.emily.cloud.gateway.utils.DataBufferUtils.dataBufferToString(body);
        }
        return bodyString;
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
}
