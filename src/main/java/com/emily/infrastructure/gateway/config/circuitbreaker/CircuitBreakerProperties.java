package com.emily.infrastructure.gateway.config.circuitbreaker;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description :  断路器配置类
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/26 3:55 下午
 */
@ConfigurationProperties(prefix = CircuitBreakerProperties.PREFIX)
public class CircuitBreakerProperties {
    /**
     * 前缀
     */
    public static final String PREFIX = "spring.emily.gateway.circuitBreaker";
    /**
     * 断路器配置类开关，默认：true
     */
    private boolean enabled = true;
}
