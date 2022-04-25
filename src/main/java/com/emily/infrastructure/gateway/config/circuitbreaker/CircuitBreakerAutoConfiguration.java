package com.emily.infrastructure.gateway.config.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
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
    /**
     * 断路器默认配置
     * @return
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer(CircuitBreakerRegistry circuitBreakerRegistry, TimeLimiterRegistry timeLimiterRegistry) {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                // 滑动窗口的类型为时间窗口，默认：COUNT_BASED
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                // 时间窗口的大小为100（COUNT_BASED-次数，TIME_BASED-秒），默认：100
                .slidingWindowSize(100)
                // 断路器故障率阀值（百分比），大于等于阀值会将断路器置为打开状态，默认：50
                .failureRateThreshold(50)
                // 配置调用持续时间阀值，超过这个阀值的调用将被视为慢查询，并增加慢查询百分比，默认：60秒
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                // 慢查询百分比，默认：100（百分比）
                .slowCallRateThreshold(40)
                // 在单个滑动窗口计算故障率或慢查询率之前最小的调用次数，默认：100
                .minimumNumberOfCalls(10)
                // （可选参数）配置CircuitBreaker断路器由half open状态转为open状态之前需保持的时间，
                // 默认断路器将会保持half open状态直到minimumNumberOfCalls指定的次数（无论成功还是失败）
                //.maxWaitDurationInHalfOpenState(Duration.ofSeconds(1))
                // 在HALF-OPEN(半开状态)下允许进行正常调用的次数，默认：10
                .permittedNumberOfCallsInHalfOpenState(10)
                // 断路器由打开状态转换为半开状态需要时间，默认：60秒
                .waitDurationInOpenState(Duration.ofSeconds(60))
                // 允许断路器自动由OPEN状态转为HALF_OPEN半开状态
                .enableAutomaticTransitionFromOpenToHalfOpen()
                // 指定记录为失败的异常列表，只要匹配或其子类都会记录为异常
                .recordExceptions(Throwable.class)
                // 自定义异常列表，即不会计入成功也不会计入失败（即使在recordExceptions列表中指定）
                .ignoreExceptions()
                // 自定义断言，如果异常要被统计，则返回true，否则返回false (recordFailure)
                .recordException(throwable -> true)
                // 自定义断言，如果异常即不计入成功也不计入失败，则返回true，否则返回false
                .ignoreException(throwable -> false)
                .build();


        return factory -> factory.configureDefault(id-> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(circuitBreakerConfig)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(1))
                        .cancelRunningFuture(true).build())
                .build());

    }
}
