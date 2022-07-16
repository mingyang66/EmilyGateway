package com.emily.infrastructure.gateway.config.filter;

import com.emily.infrastructure.gateway.config.filter.dedupe.DedupeLoginGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description :  过滤器配置类
 * @Author :  Emily
 * @CreateDate :  Created in 2022/7/11 11:09 上午
 */
@Configuration
public class GatewayFilterAutoConfiguration {
    /**
     * 剔除所有访问接口登录状态
     *
     * @return
     */
    @Bean
    public DedupeLoginGatewayFilterFactory dedupeLoginRoutePredicateFactory() {
        return new DedupeLoginGatewayFilterFactory();
    }
}
