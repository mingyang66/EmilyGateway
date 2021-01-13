package com.emily.cloud.gateway.filter.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @program: EmilyGateway
 * @description: 限流依据 IP地址
 * @create: 2020/12/31
 */
public class IpAddressKeyResolver implements KeyResolver {
    /**
     * Bean名称
     */
    public static final String BEAN_NAME = "ipAddressKeyResolver";

    /**
     * 获取请求的IP地址
     */
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }
}
