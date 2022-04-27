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
    public static final String PREFIX = "spring.emily.gateway.circuit-breaker";
    /**
     * 断路器配置类开关，默认：true
     */
    private boolean enabled = true;
    /**
     * 超时时间，默认：1s
     */
    private long timeout = 1;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
