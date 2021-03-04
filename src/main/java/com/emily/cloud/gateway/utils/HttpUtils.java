package com.emily.cloud.gateway.utils;

import com.emily.framework.common.enums.AppHttpStatus;
import com.emily.framework.common.exception.BusinessException;
import com.emily.framework.common.utils.json.JSONUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @program: EmilyGateway
 * @description: 请求工具类
 * @author: 姚明洋
 * @create: 2021/03/04
 */
public class HttpUtils {
    /**
     * 获取网关请求IP
     */
    public static String getIp(ServerHttpRequest request) {
        if (Objects.isNull(request)) {
            throw new BusinessException(AppHttpStatus.ILLEGAL_ARGUMENT_EXCEPTION);
        }
        return request.getRemoteAddress().getHostString();
    }

    /**
     * 获取请求路径
     */
    public static String getPath(ServerHttpRequest request) {
        if (Objects.isNull(request)) {
            throw new BusinessException(AppHttpStatus.ILLEGAL_ARGUMENT_EXCEPTION);
        }
        return request.getPath().value();
    }

    /**
     * 网关请求协议
     */
    public static String getSchema(ServerHttpRequest request) {
        if (Objects.isNull(request)) {
            throw new BusinessException(AppHttpStatus.ILLEGAL_ARGUMENT_EXCEPTION);
        }
        return request.getURI().getScheme();
    }

    /**
     * 获取请求Method
     */
    public static String getMethod(ServerHttpRequest request) {
        if (Objects.isNull(request)) {
            throw new BusinessException(AppHttpStatus.ILLEGAL_ARGUMENT_EXCEPTION);
        }
        return request.getMethodValue();
    }

    /**
     * 获取请求ContentType
     *
     * @param request
     * @return
     */
    public static String getContentType(ServerHttpRequest request) {
        if (Objects.isNull(request)) {
            throw new BusinessException(AppHttpStatus.ILLEGAL_ARGUMENT_EXCEPTION);
        }
        return request.getHeaders().getContentType() == null ? null : MediaType.toString(Arrays.asList(request.getHeaders().getContentType()));
    }
}
