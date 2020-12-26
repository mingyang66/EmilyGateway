package com.emily.cloud.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @program: spring-cloud-gateway
 * @description:
 * @author: 姚明洋
 * @create: 2020/12/22
 */
@Component
public class EmilyFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("-----------url:"+exchange.getRequest().getId());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
