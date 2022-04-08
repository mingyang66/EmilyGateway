package com.emily.infrastructure.gateway.config;

import com.emily.infrastructure.gateway.filter.EmilyExternalGlobalFilter;
import com.emily.infrastructure.gateway.filter.EmilyLogGlobalFilter;
import com.emily.infrastructure.gateway.filter.EmilyRetryGlobalFilter;
import com.emily.infrastructure.gateway.filter.EmilySchemaGlobalFilter;
import com.emily.infrastructure.gateway.filter.factory.EmilyExternalGatewayFilterFactory;
import com.emily.infrastructure.gateway.filter.ratelimit.IpAddressKeyResolver;
import com.emily.infrastructure.gateway.route.RedisRouteDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.cloud.gateway.event.EnableBodyCachingEvent;
import org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.Set;

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
     * 网关支持的协议过滤器
     */
    @Bean
    public EmilySchemaGlobalFilter emilySchemaGlobalFilter(EmilyGatewayProperties emilyGatewayProperties){
        EmilySchemaGlobalFilter emilySchemaGlobalFilter = new EmilySchemaGlobalFilter(emilyGatewayProperties);
        emilySchemaGlobalFilter.setOrder(AdaptCachedBodyGlobalFilter.HIGHEST_PRECEDENCE+2000);
        return emilySchemaGlobalFilter;
    }
    /**
     * 限制指定路由只能内网访问过滤器
     */
    @Bean
    public EmilyExternalGlobalFilter emilyExternalGlobalFilter(EmilyGatewayProperties emilyGatewayProperties) {
        EmilyExternalGlobalFilter emilyExternalGlobalFilter = new EmilyExternalGlobalFilter(emilyGatewayProperties);
        //将限制放在将路由转换到URL过滤器之后
        emilyExternalGlobalFilter.setOrder(RouteToRequestUrlFilter.HIGHEST_PRECEDENCE + 3000);
        return emilyExternalGlobalFilter;
    }

    /**
     * 注册请求响应日志拦截全局过滤器
     */
    @Bean
    public EmilyLogGlobalFilter emilyLogGlobalFilter(EmilyGatewayProperties emilyGatewayProperties, Set<MessageBodyDecoder> messageBodyDecoders) {
        EmilyLogGlobalFilter emilyLogGlobalFilter = new EmilyLogGlobalFilter(emilyGatewayProperties, messageBodyDecoders);
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
     * 自定义私有限制外网访问过滤器
     */
    @Bean
    @ConditionalOnEnabledFilter
    public EmilyExternalGatewayFilterFactory emilyExternalGatewayFilterFactory() {
        //将限制放在将路由转换到URL过滤器之后
        return new EmilyExternalGatewayFilterFactory(RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER+2);
    }

    /**
     * 限流key
     */
    @Bean(IpAddressKeyResolver.BEAN_NAME)
    public IpAddressKeyResolver ipAddressKeyResolver() {
        return new IpAddressKeyResolver();
    }

    /**
     * 初始化自定义路由仓储
     */
    @Bean
    @ConditionalOnMissingBean(RouteDefinitionRepository.class)
    public RedisRouteDefinitionRepository redisRouteDefinitionRepository(RedisTemplate redisTemplate){
        return new RedisRouteDefinitionRepository(redisTemplate);
    }
}
