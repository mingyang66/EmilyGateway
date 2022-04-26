package com.emily.infrastructure.gateway.config.exception;

import com.emily.infrastructure.gateway.config.exception.handler.GatewayErrorWebExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @Description :  网关异常处理配置类
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/26 3:27 下午
 */
@Configuration
@EnableConfigurationProperties(GatewayWebExceptionProperties.class)
@ConditionalOnProperty(prefix = GatewayWebExceptionProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayWebExceptionAutoConfiguration {

    @Bean
    @Order(-1)
    public GatewayErrorWebExceptionHandler gatewayErrorWebExceptionHandler(){
        return new GatewayErrorWebExceptionHandler();
    }
}
