package com.emily.infrastructure.gateway.config.logger.filter;

import com.emily.infrastructure.common.utils.json.JSONUtils;
import com.emily.infrastructure.gateway.common.entity.BaseLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * @Description :  网关记录日志装饰器类
 * @Author :  Emily
 * @CreateDate :  Created in 2022/4/28 5:51 下午
 */
public class RecordLoggerResponseDecorator extends ServerHttpResponseDecorator {
    private ServerWebExchange exchange;

    public RecordLoggerResponseDecorator(ServerWebExchange exchange) {
        super(exchange.getResponse());
        this.exchange = exchange;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        if (body instanceof Flux) {
            Flux<DataBuffer> fluxBody = (Flux<DataBuffer>) body;
            return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                DataBuffer join = dataBufferFactory.join(dataBuffers);
                byte[] content = new byte[join.readableByteCount()];
                join.read(content);
                DataBufferUtils.release(join);

                String bodyStr = new String(content, StandardCharsets.UTF_8);
                // 获取记录日志对象
                BaseLogger baseLogger = exchange.getAttribute(RecordLoggerGatewayFilterFactory.BASE_LOGGER);
                //设置请求URL
                baseLogger.setUrl(exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR).toString());
                //设置响应结果
                baseLogger.setResponseBody(transBody(exchange.getResponse().getHeaders().getContentType(), bodyStr));
                this.getDelegate().getHeaders().setContentLength(bodyStr.getBytes().length);
                return bufferFactory().wrap(bodyStr.getBytes());
            }));
        }
        return super.writeWith(body);
    }

    /**
     * 将数据转换为对象类型
     *
     * @param body     字符串
     */

    private Object transBody(MediaType mediaType, String body) {
        if (StringUtils.isEmpty(body)) {
            return body;
        }
        if (mediaType.includes(MediaType.APPLICATION_JSON) || mediaType.includes(MediaType.APPLICATION_JSON_UTF8)) {
            try {
                return JSONUtils.toJavaBean(body, Map.class);
            } catch (Exception e) {
                try {
                    return JSONUtils.toJavaBean(body, new TypeReference<List<Map<Object, Object>>>() {
                    });
                } catch (Exception exception) {
                }
            }
        }
        return body;
    }
}
