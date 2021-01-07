package com.emily.cloud.gateway.exception;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.stream.Collectors;

/**
 * @program: EmilyGateway
 * @description: 自定义异常处理自动化配置类
 * @author: 姚明洋
 * @create: 2021/01/06
 */
@ControllerAdvice
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(WebFluxConfigurer.class)
@AutoConfigureBefore(ErrorWebFluxAutoConfiguration.class)
@EnableConfigurationProperties({ ServerProperties.class,
        org.springframework.boot.autoconfigure.web.ResourceProperties.class, WebProperties.class })
public class EmilyErrorWebFluxAutoConfiguration {

    private final ServerProperties serverProperties;

    public EmilyErrorWebFluxAutoConfiguration(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorWebExceptionHandler.class, search = SearchStrategy.CURRENT)
    @Order(-2)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes,
                                                             org.springframework.boot.autoconfigure.web.ResourceProperties resourceProperties,
                                                             WebProperties webProperties, ObjectProvider<ViewResolver> viewResolvers,
                                                             ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
        EmilyErrorWebExceptionHandler exceptionHandler = new EmilyErrorWebExceptionHandler(errorAttributes,
                resourceProperties.hasBeenCustomized() ? resourceProperties : webProperties.getResources(),
                this.serverProperties.getError(), applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }

}
