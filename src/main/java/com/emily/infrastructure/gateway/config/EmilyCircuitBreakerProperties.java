package com.emily.infrastructure.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @program: EmilyGateway
 * @description: 断路器配置文件
 * @author:
 * @create: 2021/03/09
 */
@ConfigurationProperties(prefix = "spring.emily.gateway.circuitbreaker")
public class EmilyCircuitBreakerProperties {
    /**
     * 超时时间，默认：1000毫秒
     */
    private long timeout = 1000;

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
