package com.emily.cloud.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @program: EmilyGateway
 * @description: 网关断路器自动化配置
 * @create: 2021/03/06
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EmilyCircuitBreakerProperties.class)
public class EmilyCircuitBreakerAutoConfiguration {

    @Bean
    public ReactiveResilience4JCircuitBreakerFactory defaultCustomizer(EmilyCircuitBreakerProperties emilyCircuitBreakerProperties) {

        ReactiveResilience4JCircuitBreakerFactory factory = new ReactiveResilience4JCircuitBreakerFactory();
        //默认超时规则,默认1s,不使用断路器超时规则可以设置大一点
        factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(emilyCircuitBreakerProperties.getTimeout())).build())
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .build());
        return factory;
    }
}
