package com.emily.cloud.gateway.filter;

import com.emily.cloud.gateway.entity.LogEntity;
import com.emily.cloud.gateway.utils.DataBufferUtils;
import com.emily.cloud.gateway.utils.GZIPUtils;
import com.emily.cloud.gateway.utils.JSONUtils;
import com.emily.cloud.gateway.utils.enums.DateFormatEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * @program: EmilyGateway
 * @description: 网关全局过滤器，拦截请求响应日志
 * @create: 2020/12/22
 */
public class EmilyLogGlobalFilter implements GlobalFilter, Ordered {

    private static Logger logger = LoggerFactory.getLogger(EmilyLogGlobalFilter.class);
    /**
     * 优先级顺序
     */
    private int order;

    /**
     * 响应数据属性KEY
     */
    private String EMILY_RESPONSE_BODY = "EMILY_RESPONSE_BODY";
    /**
     * 日志实体对象
     */
    private String EMILY_LOG_ENTITY = "EMILY_LOG_ENTITY";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(EMILY_LOG_ENTITY, new LogEntity(exchange));
        return chain.filter(exchange.mutate().response(getServerHttpResponseDecorator(exchange)).build())
                .doOnError(throwable -> doLogException(exchange))
                .then(Mono.defer(() -> doLogResponse(exchange)))
                .doOnCancel(() -> System.out.println("---------doOnCancel----------"));
    }

    /**
     *
     * @param exchange
     */
    protected void doLogException(ServerWebExchange exchange) {
        System.out.println("-------------doException--------");
    }

    /**
     * 记录请求和响应日志
     *
     * @param exchange
     * @return
     */
    protected Mono<Void> doLogResponse(ServerWebExchange exchange) {
        LogEntity logEntity = exchange.getAttribute(EMILY_LOG_ENTITY);
        logEntity.setUrl(exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR).toString());
        logEntity.setResponseDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormatEnum.YYYY_MM_DD_HH_MM_SS_SSS.getFormat())));
        // 响应数据类型
        MediaType mediaType = exchange.getResponse().getHeaders().getContentType();
        if (mediaType != null) {
            logEntity.setResponseContentType(MediaType.toString(Arrays.asList(mediaType)));
        }
        // 响应数据
        logEntity.setData(exchange.getAttribute(EMILY_RESPONSE_BODY));
        logger.info(JSONUtils.toJSONString(logEntity));
        return Mono.empty();
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
                        // 将DataBuffer转换为字节数组，兼容分片传输
                        byte[] allBytes = DataBufferUtils.dataBufferToByte(dataBuffers);
                        // 获取响应body
                        String bodyString;
                        if (headers.containsKey(HttpHeaders.CONTENT_ENCODING) && "gzip".equalsIgnoreCase(headers.get(HttpHeaders.CONTENT_ENCODING).get(0))) {
                            bodyString = GZIPUtils.decompressToString(allBytes);
                        } else {
                            bodyString = new String(allBytes, StandardCharsets.UTF_8);
                        }
                        // 设置响应body
                        exchange.getAttributes().put(EMILY_RESPONSE_BODY, convertToObj(exchange, bodyString));
                        return exchange.getResponse().bufferFactory().wrap(allBytes);
                    }));
                }
                return super.writeWith(body);
            }
        };
    }

    /**
     * 将数据转换为对象类型
     *
     * @param exchange 网关上线文
     * @param body     字符串
     */
    protected Object convertToObj(ServerWebExchange exchange, String body) {
        if (StringUtils.isEmpty(body)) return null;
        // 响应数据类型
        MediaType mediaType = exchange.getResponse().getHeaders().getContentType();
        if ((mediaType.includes(MediaType.APPLICATION_JSON)
                || mediaType.includes(MediaType.APPLICATION_JSON_UTF8))
                && StringUtils.isNotEmpty(body)) {
            if (StringUtils.startsWith(body, "[")) {
                return JSONUtils.toJavaBean(body, new TypeReference<List<Map<Object, Object>>>() {
                });
            }
            return JSONUtils.toJavaBean(body, Map.class);
        }
        return body;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
