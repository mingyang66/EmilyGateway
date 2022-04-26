package com.emily.infrastructure.gateway.config.exception.handler;

import com.emily.infrastructure.common.utils.json.JSONUtils;
import com.emily.infrastructure.gateway.common.entity.BaseResponse;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;

/**
 * @Description :  网关全局异常处理
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/26 2:57 下午
 */
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    /**
     * 处理给定的异常
     * @param exchange
     * @param ex
     * @return
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        BaseResponse baseResponse = new BaseResponse();
        if (ex instanceof ResponseStatusException) {
            baseResponse.setStatus(((ResponseStatusException) ex).getStatus().value());
            baseResponse.setMessage(((ResponseStatusException) ex).getReason());
        } else {
            baseResponse.setStatus(500);
            baseResponse.setMessage(ex.getMessage());
        }
        DataBuffer dataBuffer = response.bufferFactory()
                .allocateBuffer().write(JSONUtils.toJSONString(baseResponse).getBytes());
        response.setStatusCode(HttpStatus.OK);
        //基于流形式
        response.getHeaders().setContentType(MediaType.APPLICATION_NDJSON);
        return response.writeAndFlushWith(Mono.just(ByteBufMono.just(dataBuffer)));
    }
}
