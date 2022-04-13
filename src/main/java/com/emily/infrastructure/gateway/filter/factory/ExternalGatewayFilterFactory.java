package com.emily.infrastructure.gateway.filter.factory;

import com.emily.infrastructure.common.enums.AppHttpStatus;
import com.emily.infrastructure.common.exception.BusinessException;
import com.emily.infrastructure.common.utils.RequestUtils;
import com.emily.infrastructure.common.utils.path.PathMatcher;
import com.emily.infrastructure.gateway.common.HttpUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Emily
 * @program: EmilyGateway
 * @description: 外部网络访问服务私有过滤器
 * @create: 2021/03/03
 */
public class ExternalGatewayFilterFactory extends AbstractGatewayFilterFactory<ExternalGatewayFilterFactory.Config> {
    /**
     * 外网限制组件名
     */
    public static final String NAME = "EmilyExternal";
    private static final String ENABLE = "enable";
    private static final String PATH = "path";
    private int order;

    public ExternalGatewayFilterFactory(int order) {
        super(Config.class);
        this.order = order;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(ENABLE, PATH);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new EmilyGatewayFilter(config, order);
    }

    private static class EmilyGatewayFilter implements GatewayFilter, Ordered {
        private Config config;
        private int order;

        public EmilyGatewayFilter(Config config, int order) {
            this.config = config;
            this.order = order;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            if (determineExternalLimit(exchange, config)) {
                throw new BusinessException(AppHttpStatus.SERVER_ILLEGAL_ACCESS);
            }
            return chain.filter(exchange);
        }

        /**
         * 判定外部网路限制
         *
         * @param exchange 网关上下文
         */
        protected boolean determineExternalLimit(ServerWebExchange exchange, Config config) {
            if (!config.isEnable()) {
                return false;
            }
            //判定是否是内网，内网不做限制
            if (RequestUtils.isInternet(HttpUtils.getIp(exchange.getRequest()))) {
                return false;
            }
            // 开启外部网络限制，具体地址未配置，则全部限制
            if (config.getPath().isEmpty()) {
                return true;
            }
            //获取请求路由
            String path = HttpUtils.getPath(exchange.getRequest());
            /**
             * Ant表达式匹配类
             */
            PathMatcher pathMatcher = new PathMatcher(config.getPath().toArray(new String[]{}));
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

    @Override
    public String name() {
        return NAME;
    }

    public static class Config {
        /**
         * 开启外网限制
         */
        private boolean enable = true;
        /**
         * 路由
         */
        private Set<String> path = new HashSet<>();

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public Set<String> getPath() {
            return path;
        }

        public void setPath(Set<String> path) {
            this.path = path;
        }
    }
}
