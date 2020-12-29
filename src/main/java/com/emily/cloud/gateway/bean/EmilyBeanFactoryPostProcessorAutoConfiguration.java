package com.emily.cloud.gateway.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: EmilyGateway
 * @description:
 * @create: 2020/09/11
 */
@Configuration(proxyBeanMethods = false)
public class EmilyBeanFactoryPostProcessorAutoConfiguration {

    @Bean
    public static EmilyBeanFactoryPostProcessor grainBeanFactoryPostProcessor(){
        return new EmilyBeanFactoryPostProcessor();
    }
}
