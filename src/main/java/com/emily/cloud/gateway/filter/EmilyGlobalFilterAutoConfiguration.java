package com.emily.cloud.gateway.filter;

import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
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
        EmilyGlobalFilter emilyGlobalFilter = new EmilyGlobalFilter();
        //设置优先级顺序在{@link org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter}(10150)过滤器之后，方便获取到真实的请求地址
        emilyGlobalFilter.setOrder(ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER+1);
        return emilyGlobalFilter;
    }
}
