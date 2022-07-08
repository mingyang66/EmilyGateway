package com.emily.infrastructure.gateway.config.predicate;

import com.emily.infrastructure.gateway.config.predicate.path.PathOffRoutePredicateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description :  断言工厂配置类
 * @Author :  姚明洋
 * @CreateDate :  Created in 2022/7/7 10:38 上午
 */
@Configuration
public class PredicateFactoryAutoConfiguration {

    @Bean
    public PathOffRoutePredicateFactory pathOffRoutePredicateFactory() {
        return new PathOffRoutePredicateFactory();
    }
}
