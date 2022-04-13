package com.emily.infrastructure.gateway.filter;

import com.emily.infrastructure.common.enums.AppHttpStatus;
import com.emily.infrastructure.common.exception.BusinessException;
import com.emily.infrastructure.gateway.common.HttpUtils;
import com.emily.infrastructure.gateway.config.GatewayBeanProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @program: EmilyGateway
 * @description: 网关协议过滤器
 * @author:
 * @create: 2021/03/05
 */
public class SchemaGlobalFilter implements GlobalFilter, Ordered {

    private int order;

    private GatewayBeanProperties emilyGatewayProperties;

    public SchemaGlobalFilter(GatewayBeanProperties emilyGatewayProperties) {
        this.emilyGatewayProperties = emilyGatewayProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (emilyGatewayProperties.getIncludeSchemas().contains(HttpUtils.getSchema(exchange.getRequest()))) {
            return chain.filter(exchange);
        }
        throw new BusinessException(AppHttpStatus.SERVER_ILLEGAL_ACCESS);
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
