package com.emily.infrastructure.gateway.config.circuitbreaker.controller;

import com.emily.infrastructure.gateway.common.entity.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

    @RequestMapping(value = "fallback", method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.PUT, RequestMethod.DELETE})
    public BaseResponse fallback(ServerWebExchange exchange) {
        Throwable throwable = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        ServerWebExchange delegate = ((ServerWebExchangeDecorator) exchange).getDelegate();
        logger.error("服务调用失败，URL={}", delegate.getRequest().getURI(), throwable);
        return BaseResponse.build(HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
    }
}
