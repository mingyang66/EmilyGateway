package com.emily.infrastructure.gateway.config.filter.dedupe;

import com.emily.infrastructure.gateway.common.utils.FilterUtils;
import com.emily.infrastructure.gateway.config.filter.order.GatewayFilterOrdered;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * @Description :  剔除登录断言
 * @Author :  Emily
 * @CreateDate :  Created in 2022/7/7 10:30 上午
 */
public class DedupeLoginGatewayFilterFactory extends AbstractGatewayFilterFactory<DedupeLoginGatewayFilterFactory.Config> {

    private static final String WHITE_LIST = "whiteList";

    public DedupeLoginGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(WHITE_LIST);
    }


    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilterOrdered() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                String[] paths = StringUtils.split(exchange.getRequest().getURI().getRawPath(), "/");
                if (config.getWhiteList().contains(paths[0])) {
                    return chain.filter(exchange);
                }
                ServerHttpResponse response = exchange.getResponse();
                //返回结果
                DataBuffer dataBuffer = FilterUtils.getResponseData(exchange);
                //指定编码，否则在浏览器中会中文乱码
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                return response.writeWith(Mono.just(dataBuffer));
            }

            @Override
            public int getOrder() {
                return Ordered.HIGHEST_PRECEDENCE;
            }
        };
    }

    @Validated
    public static class Config {

        private List<String> whiteList;

        public List<String> getWhiteList() {
            return whiteList;
        }

        public void setWhiteList(List<String> whiteList) {
            this.whiteList = whiteList;
        }
    }

}
