package com.emily.infrastructure.gateway.filter;

import com.emily.infrastructure.common.enums.AppHttpStatus;
import com.emily.infrastructure.common.exception.BusinessException;
import com.emily.infrastructure.common.utils.RequestUtils;
import com.emily.infrastructure.common.utils.path.PathMatcher;
import com.emily.infrastructure.gateway.common.HttpUtils;
import com.emily.infrastructure.gateway.config.GatewayBeanProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * @program: EmilyGateway
 * @description: 路由内网路由限制全局过滤器
 * @create: 2021/03/03
 */
public class ExternalGlobalFilter implements GlobalFilter, Ordered {

    private GatewayBeanProperties emilyGatewayProperties;
    /**
     * 网关过滤器执行顺序
     */
    private int order;

    public ExternalGlobalFilter(GatewayBeanProperties emilyGatewayProperties) {
        this.emilyGatewayProperties = emilyGatewayProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (determineExternalLimit(exchange)) {
            throw new BusinessException(AppHttpStatus.SERVER_ILLEGAL_ACCESS);
        }
        return chain.filter(exchange);
    }

    /**
     * 判定外部网路限制
     *
     * @param exchange 网关上下文
     */
    protected boolean determineExternalLimit(ServerWebExchange exchange) {
        //判定是否是内网，内网不做限制
        if(RequestUtils.isInternet(HttpUtils.getIp(exchange.getRequest()))){
            return false;
        }
        //获取网关路由配置信息
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        // 获取无需记录日志的路由配置信息
        GatewayBeanProperties.External excludeExternelRoute = emilyGatewayProperties.getExcludeExternelRoute(route.getId());
        // 路由ID未配置，则不限制
        if (Objects.isNull(excludeExternelRoute)) {
            return false;
        }
        // 路由ID配置，路由未配置，则全部限制
        if (excludeExternelRoute.getPath().isEmpty()) {
            return true;
        }
        /**
         * Ant表达式匹配类
         */
        PathMatcher pathMatcher = new PathMatcher(excludeExternelRoute.getPath().toArray(new String[]{}));
        //获取请求路由
        String path = HttpUtils.getPath(exchange.getRequest());
        if (pathMatcher.match(path)) {
            return true;
        }
        return false;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
