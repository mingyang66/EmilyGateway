package com.emily.cloud.gateway.filter;

import com.emily.cloud.gateway.config.EmilyGatewayProperties;
import com.emily.cloud.gateway.utils.HttpUtils;
import com.emily.framework.common.enums.AppHttpStatus;
import com.emily.framework.common.exception.BusinessException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @program: EmilyGateway
 * @description: 网关协议过滤器
 * @author: 姚明洋
 * @create: 2021/03/05
 */
public class EmilySchemaGlobalFilter implements GlobalFilter, Ordered {

    private int order;

    private EmilyGatewayProperties emilyGatewayProperties;

    public EmilySchemaGlobalFilter(EmilyGatewayProperties emilyGatewayProperties) {
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
