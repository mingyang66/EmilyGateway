package com.emily.infrastructure.gateway.config.logger.filter;

import com.emily.infrastructure.common.utils.json.JSONUtils;
import com.emily.infrastructure.gateway.common.entity.BaseLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.EnableBodyCachingEvent;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * @author Emily
 * @program: EmilyGateway
 * @description: 网关全局过滤器，拦截请求响应日志
 * @create: 2020/12/22
 */
public class RecordLoggerGatewayFilterFactory extends AbstractGatewayFilterFactory<RecordLoggerGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RecordLoggerGatewayFilterFactory.class);

    public static final String KEY_PREFIX = "enabled";
    public static final String KEY_EXCLUDE_PATH = "excludePath";
    /**
     * 日志实体对象
     */
    public static final String BASE_LOGGER = "BASE_LOGGER";
    /**
     * 请求开始时间
     */
    public static final String START_TIME = "START_TIME";

    public RecordLoggerGatewayFilterFactory() {
        super(RecordLoggerGatewayFilterFactory.Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(KEY_PREFIX, KEY_EXCLUDE_PATH);
    }

    @Override
    public GatewayFilter apply(RecordLoggerGatewayFilterFactory.Config config) {

        return new GatewayFilterOrdered() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // 判定是否开启日志记录
                if (!config.isEnabled()) {
                    return chain.filter(exchange);
                }
                Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
                if (route != null && getPublisher() != null) {
                    // send an event to enable caching
                    getPublisher().publishEvent(new EnableBodyCachingEvent(this, route.getId()));
                }
                exchange.getAttributes().put(BASE_LOGGER, new BaseLogger(exchange));
                exchange.getAttributes().put(START_TIME, System.currentTimeMillis());
                /**
                 * 获取响应结果有两种方案：
                 * 1.就是如下自定义修饰类的方式获取响应结果
                 * 2.通过重写 {@link ModifyResponseBodyGatewayFilterFactory.ModifiedServerHttpResponse}修饰类的方式来实现
                 */
                return chain.filter(exchange.mutate().response(new RecordLoggerResponseDecorator(exchange)).build())
                        //如果Mono在没有数据的情况下完成，则要调用的回调参数为null
                        .doOnSuccess((args) -> {
                            BaseLogger baseLogger = exchange.getAttribute(BASE_LOGGER);
                            // 设置响应时间
                            baseLogger.setTime(System.currentTimeMillis() - exchange.getAttributeOrDefault(START_TIME, 0L));
                            // 记录日志信息
                            logger.info(JSONUtils.toJSONString(baseLogger));
                        })
                        // 当Mono完成并出现错误时触发，将会发送onError信号
                        .doOnError(throwable -> {
                            BaseLogger baseLogger = exchange.getAttribute(BASE_LOGGER);
                            // 设置响应时间
                            baseLogger.setTime(System.currentTimeMillis() - exchange.getAttributeOrDefault(START_TIME, 0L));
                            // 设置返回的错误信息
                            baseLogger.setResponseBody(throwable.getMessage());
                            // 记录日志信息
                            logger.error(JSONUtils.toJSONString(baseLogger));
                        });
            }

            @Override
            public int getOrder() {
                return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
            }
        };

    }


    public static class Config {
        /**
         * 是否开启日志记录，默认：true
         */
        private boolean enabled = true;
        /**
         * 排除请求URL
         */
        private Set<String> excludePath = new HashSet<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Set<String> getExcludePath() {
            return excludePath;
        }

        public void setExcludePath(Set<String> excludePath) {
            this.excludePath = excludePath;
        }
    }
}
