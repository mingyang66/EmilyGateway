package com.emily.cloud.gateway.config;

import com.emily.cloud.gateway.filter.EmilyExternalGlobalFilter;
import com.emily.cloud.gateway.filter.EmilyLogGlobalFilter;
import com.emily.cloud.gateway.filter.EmilyRetryGlobalFilter;
import com.emily.cloud.gateway.filter.ratelimit.IpAddressKeyResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.event.EnableBodyCachingEvent;
import org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @program: EmilyGateway
 * @description: 网关全局过滤器配置
 * @create: 2020/12/29
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EmilyGatewayProperties.class)
public class EmilyGatewayAutoConfiguration {

    @Autowired
    private AdaptCachedBodyGlobalFilter adaptCachedBodyGlobalFilter;
    @Autowired
    private GatewayProperties gatewayProperties;

    /**
     * 让所有请求的body都做body cache
     */
    @PostConstruct
    public void init() {
        gatewayProperties.getRoutes().forEach(routeDefinition -> {
            adaptCachedBodyGlobalFilter.onApplicationEvent(new EnableBodyCachingEvent(this, routeDefinition.getId()));
        });
    }

    /**
     * 限制指定路由只能内网访问过滤器
     */
    @Bean
    public EmilyExternalGlobalFilter emilyExternalGlobalFilter(EmilyGatewayProperties emilyGatewayProperties){
        EmilyExternalGlobalFilter emilyExternalGlobalFilter = new EmilyExternalGlobalFilter(emilyGatewayProperties);
        //设置优先级，优先级要在日志拦截器之前
        emilyExternalGlobalFilter.setOrder(NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 2);
        return emilyExternalGlobalFilter;
    }
    /**
     * 注册请求响应日志拦截全局过滤器
     */
    @Bean
    public EmilyLogGlobalFilter emilyLogGlobalFilter(EmilyGatewayProperties emilyGatewayProperties) {
        EmilyLogGlobalFilter emilyLogGlobalFilter = new EmilyLogGlobalFilter(emilyGatewayProperties);
        //设置优先级顺序在{@link org.springframework.cloud.gateway.filter.NettyWriteResponseFilter}(-1)过滤器之后，方便获取到真实的请求地址
        emilyLogGlobalFilter.setOrder(NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1);
        return emilyLogGlobalFilter;
    }

    /**
     * Retry日志拦截全局过滤器
     *
     * @return
     */
    @Bean
    public EmilyRetryGlobalFilter emilyRetryGlobalFilter() {
        EmilyRetryGlobalFilter emilyRetryGlobalFilter = new EmilyRetryGlobalFilter();
        emilyRetryGlobalFilter.setOrder(ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER - 1);
        return emilyRetryGlobalFilter;
    }

    /**
     * 限流key
     */
    @Bean(IpAddressKeyResolver.BEAN_NAME)
    public IpAddressKeyResolver ipAddressKeyResolver() {
        return new IpAddressKeyResolver();
    }
}
