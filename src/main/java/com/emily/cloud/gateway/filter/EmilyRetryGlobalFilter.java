package com.emily.cloud.gateway.filter;

import com.emily.cloud.gateway.entity.LogEntity;
import com.emily.cloud.gateway.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory.RETRY_ITERATION_KEY;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * @program: EmilyGateway
 * @description: Retry And Repeat Filter
 * @author: 姚明洋
 * @create: 2021/02/22
 */
public class EmilyRetryGlobalFilter implements GlobalFilter, Ordered {

    private static Logger logger = LoggerFactory.getLogger(EmilyRetryGlobalFilter.class);
    /**
     * 优先级顺序
     */
    public int order;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).doFinally(signalType -> {
            // 获取接口重试次数
            int iteration = exchange.getAttributeOrDefault(RETRY_ITERATION_KEY, -1);
            if (iteration > -1) {
                LogEntity logEntity = exchange.getAttributeOrDefault(EmilyLogGlobalFilter.EMILY_LOG_ENTITY, new LogEntity());
                // 设置请求URL
                URI uri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
                logEntity.setUrl(uri == null ? null : uri.toString());
                logger.info(JSONUtils.toJSONString(logEntity));
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
