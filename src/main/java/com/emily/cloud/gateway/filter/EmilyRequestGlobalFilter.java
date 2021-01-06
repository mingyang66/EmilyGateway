package com.emily.cloud.gateway.filter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @program: EmilyGateway
 * @description: 网关全局过滤器，拦截请求日志
 * @create: 2020/12/22
 */
public class EmilyRequestGlobalFilter implements GlobalFilter, Ordered {

    private static Logger logger = LoggerFactory.getLogger(EmilyRequestGlobalFilter.class);
    /**
     * 优先级顺序
     */
    private int order;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (StringUtils.endsWithIgnoreCase(HttpMethod.POST.name(), request.getMethodValue()))
            return DataBufferUtils.join(request.getBody()).flatMap(dataBuffer -> {
                String bodyString = "";
                try {
                    bodyString = IOUtils.toString(dataBuffer.asInputStream(), Charset.defaultCharset());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //释放内存
                DataBufferUtils.release(dataBuffer);
                logger.info("请求日志：" + request.getId() + "-----------url:" + request.getURI() + "--params:" + bodyString);
                return chain.filter(exchange);
            });

        logger.info("请求日志：" + request.getId() + "-----------url:" + request.getURI() + "--params:" + request.getQueryParams());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
