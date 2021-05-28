package com.emily.cloud.gateway.filter;

import com.emily.cloud.gateway.entity.LogEntity;
import com.emily.infrastructure.common.utils.json.JSONUtils;
import com.emily.infrastructure.logback.common.LoggerUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static com.emily.cloud.gateway.filter.EmilyLogGlobalFilter.EMILY_LOG_ENTITY;
import static com.emily.cloud.gateway.filter.EmilyLogGlobalFilter.EMILY_REQUEST_TIME;
import static org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory.RETRY_ITERATION_KEY;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * @program: EmilyGateway
 * @description: Retry And Repeat Filter
 * @create: 2021/02/22
 */
public class EmilyRetryGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 优先级顺序
     */
    public int order;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).doFinally(signalType -> {
            // 获取接口重试次数
            int iteration = exchange.getAttributeOrDefault(RETRY_ITERATION_KEY, 0);
            if (iteration > 0) {
                LogEntity logEntity = exchange.getAttributeOrDefault(EMILY_LOG_ENTITY, new LogEntity());
                // 设置请求URL
                URI uri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
                logEntity.setUrl(uri == null ? null : uri.toString());
                logEntity.setTime(System.currentTimeMillis() - exchange.getAttributeOrDefault(EMILY_REQUEST_TIME, 0L));
                LoggerUtils.info(EmilyRetryGlobalFilter.class, JSONUtils.toJSONString(logEntity));
            }
        });
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
