package com.emily.cloud.gateway.filter;

import com.emily.cloud.gateway.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

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
    public int order;
    /**
     * 请求参数
     */
    public static final String EMILY_REQUEST_PARAM = "EMILY_REQUEST_PARAM";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取网关请求服务地址
        ServerHttpRequest request = exchange.getRequest();
        if (request.getMethod() == HttpMethod.POST)
            return DataBufferUtils.join(request.getBody()).flatMap(dataBuffer -> {
                //新建存放响应体的字节数组
                byte[] content = new byte[dataBuffer.readableByteCount()];
                //将响应数据读取到字节数组
                dataBuffer.read(content);
                //释放内存
                DataBufferUtils.release(dataBuffer);
                // 获取请求body
                String bodyString = new String(content, StandardCharsets.UTF_8);
                //将请求参数放入网关上下文属性配置
                if (request.getHeaders().getContentType() != null && request.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
                    exchange.getAttributes().put(EMILY_REQUEST_PARAM, JSONUtils.toJavaBean(bodyString, Map.class));
                } else {
                    exchange.getAttributes().put(EMILY_REQUEST_PARAM, bodyString);
                }
                return chain.filter(exchange.mutate().request(new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return com.emily.cloud.gateway.utils.DataBufferUtils.stringToDataBuffer(bodyString);
                    }
                }).build());
            });
        //将请求参数放入网关上下文属性配置
        exchange.getAttributes().put(EMILY_REQUEST_PARAM, request.getQueryParams());
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
