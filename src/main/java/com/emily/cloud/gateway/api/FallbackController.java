package com.emily.cloud.gateway.api;

import com.emily.infrastructure.common.base.SimpleResponse;
import com.emily.infrastructure.common.enums.AppHttpStatus;
import com.emily.infrastructure.common.exception.PrintExceptionInfo;
import com.emily.infrastructure.logback.common.LoggerUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR;

/**
 * @program: EmilyGateway
 * @description: 网关熔断降级
 * @create: 2021/03/06
 */
@RestController
public class FallbackController {
    /**
     * 默认服务降级处理接口
     */
    @GetMapping("defaultFallback")
    public SimpleResponse fallback(ServerWebExchange exchange) {
        if (exchange.getAttributes().containsKey(CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR)) {
            Throwable ex = getRootCause(exchange.getAttribute(CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR));
            LoggerUtils.error(FallbackController.class, PrintExceptionInfo.printErrorInfo(ex));
        }
        return SimpleResponse.buildResponse(AppHttpStatus.SERVER_CIRCUIT_BREAKER);
    }

    private static Throwable getRootCause(final Throwable throwable) {
        final List<Throwable> list = getThrowableList(throwable);
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    private static List<Throwable> getThrowableList(Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }
}
