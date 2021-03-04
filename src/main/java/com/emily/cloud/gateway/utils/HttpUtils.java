package com.emily.cloud.gateway.utils;

import org.springframework.http.server.reactive.ServerHttpRequest;

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
    public static String getIp(ServerHttpRequest request){
        if(Objects.isNull(request)){
            return null;
        }
        return request.getRemoteAddress().getHostString();
    }

    /**
     * 获取请求路径
     */
    public static String getPath(ServerHttpRequest request){
        if(Objects.isNull(request)){
            return null;
        }
        return request.getPath().value();
    }
}
