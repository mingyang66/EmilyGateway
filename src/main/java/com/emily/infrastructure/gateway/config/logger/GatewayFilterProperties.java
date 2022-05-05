package com.emily.infrastructure.gateway.config.logger;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @program: EmilyGateway
 * @description: 网关配置类
 * @create: 2021/03/03
 */
@SuppressWarnings("all")
@ConfigurationProperties(prefix = GatewayFilterProperties.PREFIX)
public class GatewayFilterProperties {
    /**
     * 前缀
     */
    public static final String PREFIX = "spring.emily.gateway.filter";
    /**
     * 网关过滤器配置类是否开启，默认：true
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
