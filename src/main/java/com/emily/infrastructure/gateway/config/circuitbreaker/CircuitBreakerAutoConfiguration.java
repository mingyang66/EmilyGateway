package com.emily.infrastructure.gateway.config.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @Description :  弹性断路器配置类
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/20 5:28 下午
 */
@Configuration
public class CircuitBreakerAutoConfiguration {
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom() //
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED) // 滑动窗口的类型为时间窗口
                .slidingWindowSize(60) // 时间窗口的大小为60秒
                .minimumNumberOfCalls(5) // 在单位时间窗口内最少需要5次调用才能开始进行统计计算
                .failureRateThreshold(50) // 在单位时间窗口内调用失败率达到50%后会启动断路器
                .enableAutomaticTransitionFromOpenToHalfOpen() // 允许断路器自动由打开状态转换为半开状态
                .permittedNumberOfCallsInHalfOpenState(5) // 在半开状态下允许进行正常调用的次数
                .waitDurationInOpenState(Duration.ofSeconds(60)) // 断路器打开状态转换为半开状态需要等待60秒
                .recordExceptions(Throwable.class) // 所有异常都当作失败来处理
                .build();

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(circuitBreakerConfig)
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build()).build());
    }
}
