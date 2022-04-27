package com.emily.infrastructure.gateway.config.exception;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description :  网关异常处理配置类
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/26 3:29 下午
 */
@ConfigurationProperties(prefix = GatewayWebExceptionProperties.PREFIX)
public class GatewayWebExceptionProperties {
    /**
     * 默认前缀
     */
    public static final String PREFIX = "spring.emily.gateway.exception";
    /**
     * 网关全局处理开关，默认：true
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
