package com.emily.cloud.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.Charset;

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
    private int order;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取网关请求服务地址
        URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
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
                String bodyString = new String(content, Charset.forName("UTF-8"));
                logger.info("请求日志：" + request.getId() + "-----------url:" + url + "--params:" + bodyString);
                return chain.filter(exchange);
            });

        logger.info("请求日志：" + request.getId() + "-----------url:" + url + "--params:" + request.getQueryParams());
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
