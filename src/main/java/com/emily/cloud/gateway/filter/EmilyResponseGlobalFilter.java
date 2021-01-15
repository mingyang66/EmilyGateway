package com.emily.cloud.gateway.filter;

import com.emily.cloud.gateway.utils.GZIPUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @program: EmilyGateway
 * @description: 网关全局过滤器，拦截请求日志
 * @create: 2020/12/22
 */
public class EmilyResponseGlobalFilter implements GlobalFilter, Ordered {

    private static Logger logger = LoggerFactory.getLogger(EmilyResponseGlobalFilter.class);
    /**
     * 优先级顺序
     */
    private int order;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange.mutate().response(getServerHttpResponseDecorator(exchange)).build());
    }

    /**
     * 获取请求响应装饰器类
     *
     * @param exchange 网关上下文
     */
    protected ServerHttpResponseDecorator getServerHttpResponseDecorator(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                HttpHeaders headers = exchange.getResponse().getHeaders();
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                        byte[] allBytes = new byte[]{};
                        //解决分片传输多次返回问题
                        List<byte[]> list = dataBuffers.stream().map(dataBuffer -> {
                            //新建存放响应体的字节数组
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            //将响应数据读取到字节数组
                            dataBuffer.read(content);
                            //释放内存
                            DataBufferUtils.release(dataBuffer);
                            return content;
                        }).collect(toList());

                        for (int i = 0; i < list.size(); i++) {
                            allBytes = ArrayUtils.addAll(allBytes, list.get(i));
                        }
                        //获取响应body
                        String bodyString;
                        if (headers.containsKey(HttpHeaders.CONTENT_ENCODING) && "gzip".equalsIgnoreCase(headers.get(HttpHeaders.CONTENT_ENCODING).get(0))) {
                            bodyString = GZIPUtils.decompressToString(allBytes);
                        } else {
                            bodyString = new String(allBytes, StandardCharsets.UTF_8);
                        }
                        logger.info("响应日志：" + exchange.getRequest().getId() + "-----------" + bodyString);
                        return exchange.getResponse().bufferFactory().wrap(allBytes);
                    }));
                }
                return super.writeWith(body);
            }
        };
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
