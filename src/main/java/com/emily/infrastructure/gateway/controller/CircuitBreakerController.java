package com.emily.infrastructure.gateway.controller;

import com.emily.infrastructure.common.exception.PrintExceptionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

/**
 * @Description :  断路器控制器
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/21 9:54 上午
 */
@RestController
@RequestMapping("circuitBreaker")
public class CircuitBreakerController {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerController.class);

    @GetMapping("fallback")
    public String fallback(ServerWebExchange exchange){
        Exception exception = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        ServerWebExchange delegate = ((ServerWebExchangeDecorator) exchange).getDelegate();
        logger.error("服务调用失败，URL={}", delegate.getRequest().getURI(), exception);
        return "Service Unavailable";
    }
}
