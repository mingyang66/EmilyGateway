package com.emily.cloud.gateway.predicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: EmilyGateway
 * @description: 断言自动化配置类
 * @create: 2021/01/23
 */
@Configuration(proxyBeanMethods = false)
public class EmilyPredicateAutoConfiguration {
    /**
     * 根据内容判定路由是否匹配
     */
    @Bean
    public EmilyRequestBodyPredicate emilyRequestBodyPredicate(){
        return new EmilyRequestBodyPredicate();
    }
}
