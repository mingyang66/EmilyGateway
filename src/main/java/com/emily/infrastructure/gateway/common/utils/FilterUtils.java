package com.emily.infrastructure.gateway.common.utils;

import com.emily.infrastructure.common.enums.AppHttpStatus;
import com.emily.infrastructure.common.utils.json.JSONUtils;
import com.google.common.collect.Maps;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * @Description :  过滤器工具类
 * @Author :  Emily
 * @CreateDate :  Created in 2022/7/9 10:38 上午
 */
public class FilterUtils {

    private static final String LANGUAGE = "language";

    /**
     * 获取异常返回数据
     *
     * @param exchange
     * @return
     */
    public static DataBuffer getResponseData(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        Map<String, Object> dataMap = Maps.newLinkedHashMap();
        dataMap.put("status", AppHttpStatus.AUTH_EXPIRE.getStatus());
        dataMap.put("message", AppHttpStatus.AUTH_EXPIRE.getMessage());
        return response.bufferFactory().wrap(JSONUtils.toByteArray(dataMap));
    }
}
