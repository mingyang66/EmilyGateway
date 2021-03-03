package com.emily.cloud.gateway.filter;

import com.emily.cloud.gateway.config.EmilyGatewayProperties;
import com.emily.cloud.gateway.entity.LogEntity;
import com.emily.cloud.gateway.utils.DataBufferUtils;
import com.emily.framework.common.enums.DateFormatEnum;
import com.emily.framework.common.utils.calculation.GZIPUtils;
import com.emily.framework.common.utils.json.JSONUtils;
import com.emily.framework.common.utils.log.LoggerUtils;
import com.emily.framework.common.utils.path.PathMatcher;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * @program: EmilyGateway
 * @description: 网关全局过滤器，拦截请求响应日志
 * @create: 2020/12/22
 */
@SuppressWarnings("all")
public class EmilyLogGlobalFilter implements GlobalFilter, Ordered {

    private EmilyGatewayProperties emilyGatewayProperties;
    /**
     * Ant表达式匹配类
     */
    private PathMatcher pathMatcher = new PathMatcher();
    /**
     * 优先级顺序
     */
    private int order;

    /**
     * 日志实体对象
     */
    public static final String EMILY_LOG_ENTITY = "EMILY_LOG_ENTITY";

    public EmilyLogGlobalFilter(EmilyGatewayProperties emilyGatewayProperties) {
        this.emilyGatewayProperties = emilyGatewayProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //请求日志记录判定限制
        if (determineLogLimit(exchange)) {
            return chain.filter(exchange);
        }
        exchange.getAttributes().put(EMILY_LOG_ENTITY, new LogEntity(exchange));
        return chain.filter(exchange.mutate().response(getServerHttpResponseDecorator(exchange)).build())
                //如果Mono在没有数据的情况下完成，则要调用的回调参数为null
                .doOnSuccess((args) -> doLogSuccess(exchange, args))
                // 当Mono完成并出现错误时触发，将会发送onError信号
                .doOnError(throwable -> doLogError(exchange, throwable))
                // 以任何理由终止的信号类型将会传递给此消费者
                .doFinally(signalType -> LoggerUtils.info(EmilyLogGlobalFilter.class, "信号类型：" + signalType));
    }

    /**
     * 请求日志记录判定限制
     * 1.无配置，则记录日志
     * 2.只配置唯一标识，则全部不记录日志
     * 3.配置唯一标识和符合ANT表达式的路由，则只有指定的路由不记录日志
     *
     * @param routeDefinition
     * @return
     */
    protected boolean determineLogLimit(ServerWebExchange exchange) {
        //获取网关路由配置信息
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        //获取请求路由
        String path = exchange.getRequest().getPath().value();
        // 获取无需记录日志的路由配置信息
        EmilyGatewayProperties.Route excludeLoggingRoute = emilyGatewayProperties.getExcludeLoggingRoute(route.getId());
        if (Objects.isNull(excludeLoggingRoute)) {
            return false;
        }
        if (excludeLoggingRoute.getPath().isEmpty()) {
            return true;
        }
        /**
         * Ant表达式匹配类
         */
        PathMatcher pathMatcher = new PathMatcher(excludeLoggingRoute.getPath().toArray(new String[]{}));
        if (pathMatcher.match(path)) {
            return true;
        }
        return false;
    }

    /**
     * @param exchange
     */
    protected void doLogError(ServerWebExchange exchange, Throwable throwable) {
        LogEntity logEntity = exchange.getAttribute(EMILY_LOG_ENTITY);
        // 设置请求URL
        logEntity.setUrl(exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR).toString());
        // 设置响应时间
        logEntity.setEndDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormatEnum.YYYY_MM_DD_HH_MM_SS_SSS.getFormat())));
        // 设置返回的错误信息
        logEntity.setData(throwable.getMessage());
        // 记录日志信息
        LoggerUtils.error(EmilyLogGlobalFilter.class, JSONUtils.toJSONString(logEntity));
    }

    /**
     * 记录请求和响应日志
     *
     * @param exchange
     * @return
     */
    protected Mono<Void> doLogSuccess(ServerWebExchange exchange, Object args) {
        LogEntity logEntity = exchange.getAttribute(EMILY_LOG_ENTITY);
        // 设置请求URL
        logEntity.setUrl(exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR).toString());
        // 设置响应时间
        logEntity.setEndDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormatEnum.YYYY_MM_DD_HH_MM_SS_SSS.getFormat())));
        // 记录日志信息
        LoggerUtils.info(EmilyLogGlobalFilter.class, JSONUtils.toJSONString(logEntity));
        return Mono.empty();
    }

    /**
     * 获取请求响应装饰器类，参考：{@link org.springframework.cloud.gateway.filter.NettyWriteResponseFilter}
     *
     * @param exchange 网关上下文
     */
    protected ServerHttpResponseDecorator getServerHttpResponseDecorator(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                /**
                 * 解析响应数据，并将解析后的结果放入网关上下文
                 */
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                        // 将DataBuffer转换为字节数组，兼容分片传输
                        byte[] allBytes = DataBufferUtils.dataBufferToByte(dataBuffers);
                        // 获取响应body
                        String bodyString = convertToString(exchange, allBytes);
                        // 获取日志实体类
                        LogEntity logEntity = exchange.getAttribute(EMILY_LOG_ENTITY);
                        // 设置响应body
                        logEntity.setData(convertToObj(exchange, bodyString));
                        return exchange.getResponse().bufferFactory().wrap(allBytes);
                    }));
                }
                return super.writeWith(body);
            }

            /**
             * 支持如下两种数据类型：
             * MediaType.TEXT_EVENT_STREAM - 是SSE(Server-Sent Events) 是websocket的一种轻型替代方案，和websocket有如下几点不同：
             *                               SSE是使用http协议，而websocket是一种单独的协议
             *                               SSE是单向传输，只能服务端想客户端推送，websocket是双向的
             *                               SSE支持断点续传，websocket需要自己实现
             *                               SSE支持发送自定义类型消息
             * MediaType.APPLICATION_STREAM_JSON - 主要用于支持webflux
             * @param body 响应数据
             */
            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return super.writeAndFlushWith(body);
            }
        };
    }

    /**
     * 将字节数组转换为字符串
     *
     * @param exchange 网关上下文
     * @param allBytes 字节数组
     * @return 响应结果
     */
    protected String convertToString(ServerWebExchange exchange, byte[] allBytes) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        // 获取响应body
        if (headers.containsKey(HttpHeaders.CONTENT_ENCODING) && "gzip".equalsIgnoreCase(headers.get(HttpHeaders.CONTENT_ENCODING).get(0))) {
            return GZIPUtils.decompressToString(allBytes);
        }
        return new String(allBytes, StandardCharsets.UTF_8);
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
            try {
                if (StringUtils.startsWith(body, "[")) {
                    return JSONUtils.toJavaBean(body, new TypeReference<List<Map<Object, Object>>>() {
                    });
                }
                return JSONUtils.toJavaBean(body, Map.class);
            } catch (Exception e) {
            }
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
