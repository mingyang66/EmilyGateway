package com.emily.cloud.gateway.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: EmilyGateway
 * @description: 网关全局过滤器配置
 * @create: 2020/12/29
 */
@Configuration(proxyBeanMethods = false)
public class EmilyGlobalFilterAutoConfiguration {

    @Bean
    public EmilyGlobalFilter emilyGlobalFilter() {
        return new EmilyGlobalFilter();
    }
}
