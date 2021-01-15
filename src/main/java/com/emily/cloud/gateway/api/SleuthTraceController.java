package com.emily.cloud.gateway.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.web.WebFluxSleuthOperators;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

/**
 * @program: EmilyGateway
 * @description: sleuth日志追踪
 * @create: 2021/01/07
 */
@RestController
public class SleuthTraceController {

    private static Logger logger = LoggerFactory.getLogger(SleuthTraceController.class);
    @Autowired
    Tracer tracer;

    @Autowired
    CurrentTraceContext currentTraceContext;

    @GetMapping
    public void logTest(ServerWebExchange exchange) {
        WebFluxSleuthOperators.withSpanInScope(tracer, currentTraceContext, exchange, () -> logger.error("TESTABCTEST"));
    }
}
