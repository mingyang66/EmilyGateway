package com.emily.cloud.gateway.filter;

import com.emily.cloud.gateway.filter.ratelimit.IpAddressKeyResolver;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
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
    /**
     * 注册请求日志拦截全局过滤器
     */
    @Bean
    public EmilyRequestGlobalFilter emilyRequestGlobalFilter() {
        EmilyRequestGlobalFilter emilyGlobalFilter = new EmilyRequestGlobalFilter();
        //设置优先级顺序在{@link org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter}(10150)过滤器之后，方便获取到真实的请求地址
        emilyGlobalFilter.setOrder(ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 1);
        return emilyGlobalFilter;
    }

    /**
     * 注册响应日志拦截全局过滤器
     */
    @Bean
    public EmilyResponseGlobalFilter emilyResponseGlobalFilter() {
        EmilyResponseGlobalFilter emilyGlobalFilter = new EmilyResponseGlobalFilter();
        //设置优先级顺序在{@link org.springframework.cloud.gateway.filter.NettyWriteResponseFilter}(-1)过滤器之后，方便获取到真实的请求地址
        emilyGlobalFilter.setOrder(NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1);
        return emilyGlobalFilter;
    }

    /**
     * 限流key
     */
    @Bean(IpAddressKeyResolver.BEAN_NAME)
    public IpAddressKeyResolver ipAddressKeyResolver() {
        return new IpAddressKeyResolver();
    }
}