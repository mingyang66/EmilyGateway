package com.emily.infrastructure.gateway.config;

import com.emily.infrastructure.gateway.filter.ExternalGlobalFilter;
import com.emily.infrastructure.gateway.filter.LoggerGlobalFilter;
import com.emily.infrastructure.gateway.filter.RetryGlobalFilter;
import com.emily.infrastructure.gateway.filter.SchemaGlobalFilter;
import com.emily.infrastructure.gateway.filter.factory.ExternalGatewayFilterFactory;
import com.emily.infrastructure.gateway.filter.ratelimit.IpAddressKeyResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.cloud.gateway.event.EnableBodyCachingEvent;
import org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * @author Emily
 * @program: EmilyGateway
 * @description: 网关全局过滤器配置
 * @create: 2020/12/29
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GatewayBeanProperties.class)
public class GatewayBeanAutoConfiguration {

    @Autowired
    private AdaptCachedBodyGlobalFilter adaptCachedBodyGlobalFilter;
    @Autowired
    private GatewayProperties gatewayProperties;

    /**
     * 让所有请求的body都做body cache
     */
    //@PostConstruct
    public void init() {
        gatewayProperties.getRoutes().forEach(routeDefinition -> {
            adaptCachedBodyGlobalFilter.onApplicationEvent(new EnableBodyCachingEvent(this, routeDefinition.getId()));
        });
    }

    /**
     * 网关支持的协议过滤器
     */
   // @Bean
    public SchemaGlobalFilter schemaGlobalFilter(GatewayBeanProperties gatewayBeanProperties){
        SchemaGlobalFilter schemaGlobalFilter = new SchemaGlobalFilter(gatewayBeanProperties);
        schemaGlobalFilter.setOrder(AdaptCachedBodyGlobalFilter.HIGHEST_PRECEDENCE+2000);
        return schemaGlobalFilter;
    }
    /**
     * 限制指定路由只能内网访问过滤器
     */
    //@Bean
    public ExternalGlobalFilter externalGlobalFilter(GatewayBeanProperties gatewayBeanProperties) {
        ExternalGlobalFilter externalGlobalFilter = new ExternalGlobalFilter(gatewayBeanProperties);
        //将限制放在将路由转换到URL过滤器之后
        externalGlobalFilter.setOrder(RouteToRequestUrlFilter.HIGHEST_PRECEDENCE + 3000);
        return externalGlobalFilter;
    }

    /**
     * 注册请求响应日志拦截全局过滤器
     */
    //@Bean
    public LoggerGlobalFilter loggerGlobalFilter(GatewayBeanProperties gatewayBeanProperties, Set<MessageBodyDecoder> messageBodyDecoders) {
        LoggerGlobalFilter loggerGlobalFilter = new LoggerGlobalFilter(gatewayBeanProperties, messageBodyDecoders);
        //设置优先级顺序在{@link org.springframework.cloud.gateway.filter.NettyWriteResponseFilter}(-1)过滤器之后，方便获取到真实的请求地址
        loggerGlobalFilter.setOrder(NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1);
        return loggerGlobalFilter;
    }

    /**
     * Retry日志拦截全局过滤器
     *
     * @return
     */
    //@Bean
    public RetryGlobalFilter retryGlobalFilter() {
        RetryGlobalFilter retryGlobalFilter = new RetryGlobalFilter();
        retryGlobalFilter.setOrder(ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER - 1);
        return retryGlobalFilter;
    }

    /**
     * 自定义私有限制外网访问过滤器
     */
    //@Bean
    //@ConditionalOnEnabledFilter
    public ExternalGatewayFilterFactory externalGatewayFilterFactory() {
        //将限制放在将路由转换到URL过滤器之后
        return new ExternalGatewayFilterFactory(RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER+2);
    }

    /**
     * 限流key
     */
    //@Bean(IpAddressKeyResolver.BEAN_NAME)
    public IpAddressKeyResolver ipAddressKeyResolver() {
        return new IpAddressKeyResolver();
    }
}
