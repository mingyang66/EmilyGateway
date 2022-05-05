package com.emily.infrastructure.gateway.config.logger;

import com.emily.infrastructure.gateway.config.logger.filter.RecordLoggerGatewayFilterFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Emily
 * @program: EmilyGateway
 * @description: 网关全局过滤器配置
 * @create: 2020/12/29
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GatewayFilterProperties.class)
@ConditionalOnProperty(prefix = GatewayFilterProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayFilterAutoConfiguration {

    /**
     * 注册请求响应日志拦截全局过滤器
     */
    @Bean
    public RecordLoggerGatewayFilterFactory recordLoggerGatewayFilterFactory() {
        return new RecordLoggerGatewayFilterFactory();
    }
}
