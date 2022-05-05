package com.emily.infrastructure.gateway.config.exception.handler;

import com.emily.infrastructure.common.exception.PrintExceptionInfo;
import com.emily.infrastructure.common.utils.json.JSONUtils;
import com.emily.infrastructure.gateway.common.entity.BaseResponse;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.client.RestClientResponseException;
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
     *
     * @param exchange
     * @param ex
     * @return
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        BaseResponse baseResponse = new BaseResponse();
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException exception = (ResponseStatusException) ex;
            baseResponse.setStatus(exception.getStatus().value());
            baseResponse.setMessage(exception.getMessage());
            System.out.println(PrintExceptionInfo.printErrorInfo(exception));
        } else if (ex instanceof RestClientResponseException) {
            RestClientResponseException exception = (RestClientResponseException)ex;
            baseResponse.setStatus(exception.getRawStatusCode());
            baseResponse.setMessage(exception.getStatusText());
        } else {
            baseResponse.setStatus(201);
            baseResponse.setMessage(ex.getMessage());
        }
        DataBuffer dataBuffer = response.bufferFactory().wrap(JSONUtils.toByteArray(baseResponse));
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        //基于流形式
        response.getHeaders().setContentType(MediaType.APPLICATION_NDJSON);
        return response.writeAndFlushWith(Mono.just(ByteBufMono.just(dataBuffer)));
    }
}
