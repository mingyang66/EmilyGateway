死磕源码系列【springcloud gateway网关缓存请求body及删除缓存body之AdaptCachedBodyGlobalFilter和RemoveCachedBodyFilter过滤器】

> RemoveCachedBodyFilter和AdaptCachedBodyGlobalFilter是两个全局过滤器，在网关过滤器链中RemoveCachedBodyFilter优先级最高，AdaptCachedBodyGlobalFilter次之，所以每次请求发送过来先将网关上线文中的请求body删除，然后再从请求中获取body缓存到网关上线文，属性是CACHED_REQUEST_BODY_ATTR（cachedRequestBody），这样就可以直接从网关上下文中拿到请求参数，而不会出现从request中拿到之后还要回填到请求体的问题；

##### 1.AdaptCachedBodyGlobalFilter缓存请求体全局过滤器

```java
public class AdaptCachedBodyGlobalFilter implements GlobalFilter, Ordered, ApplicationListener<EnableBodyCachingEvent> {

	private ConcurrentMap<String, Boolean> routesToCache = new ConcurrentHashMap<>();

	@Override
	public void onApplicationEvent(EnableBodyCachingEvent event) {
		this.routesToCache.putIfAbsent(event.getRouteId(), true);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// the cached ServerHttpRequest is used when the ServerWebExchange can not be
		// mutated, for example, during a predicate where the body is read, but still
		// needs to be cached.
		ServerHttpRequest cachedRequest = exchange.getAttributeOrDefault(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR,
				null);
		if (cachedRequest != null) {
			exchange.getAttributes().remove(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
			return chain.filter(exchange.mutate().request(cachedRequest).build());
		}

		//
		DataBuffer body = exchange.getAttributeOrDefault(CACHED_REQUEST_BODY_ATTR, null);
		Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);

		if (body != null || !this.routesToCache.containsKey(route.getId())) {
			return chain.filter(exchange);
		}
		//此处是缓存过滤器的核心，在此工具方法中会将缓存存入网关上下文之中
		return ServerWebExchangeUtils.cacheRequestBody(exchange, (serverHttpRequest) -> {
			// don't mutate and build if same request object
			if (serverHttpRequest == exchange.getRequest()) {
				return chain.filter(exchange);
			}
			return chain.filter(exchange.mutate().request(serverHttpRequest).build());
		});
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1000;
	}

}

```

##### ServerWebExchangeUtils#cacheRequestBody工具方法

```java
	public static <T> Mono<T> cacheRequestBody(ServerWebExchange exchange,
			Function<ServerHttpRequest, Mono<T>> function) {
		return cacheRequestBody(exchange, false, function);
	}
	
 private static <T> Mono<T> cacheRequestBody(ServerWebExchange exchange, boolean cacheDecoratedRequest,
			Function<ServerHttpRequest, Mono<T>> function) {
		ServerHttpResponse response = exchange.getResponse();
		NettyDataBufferFactory factory = (NettyDataBufferFactory) response.bufferFactory();
		// 将所有的DataBuffer拼接起来，这样我们可以有一个完整的body
		return DataBufferUtils.join(exchange.getRequest().getBody())
				.defaultIfEmpty(factory.wrap(new EmptyByteBuf(factory.getByteBufAllocator())))
      	//此处decorate方法中会将缓存放入网关上下文
				.map(dataBuffer -> decorate(exchange, dataBuffer, cacheDecoratedRequest))
				.switchIfEmpty(Mono.just(exchange.getRequest())).flatMap(function);
	}

	private static ServerHttpRequest decorate(ServerWebExchange exchange, DataBuffer dataBuffer,
			boolean cacheDecoratedRequest) {
		if (dataBuffer.readableByteCount() > 0) {
			if (log.isTraceEnabled()) {
				log.trace("retaining body in exchange attribute");
			}
      //此处会将请求的body信息放入网关上下文，方便后面获取
			exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, dataBuffer);
		}

		ServerHttpRequest decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
			@Override
			public Flux<DataBuffer> getBody() {
				return Mono.<DataBuffer>fromSupplier(() -> {
					if (exchange.getAttributeOrDefault(CACHED_REQUEST_BODY_ATTR, null) == null) {
						// probably == downstream closed or no body
						return null;
					}
					// TODO: deal with Netty
					NettyDataBuffer pdb = (NettyDataBuffer) dataBuffer;
					return pdb.factory().wrap(pdb.getNativeBuffer().retainedSlice());
				}).flux();
			}
		};
		if (cacheDecoratedRequest) {
			exchange.getAttributes().put(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR, decorator);
		}
		return decorator;
	}
```

##### 2.RemoveCachedBodyFilter删除缓存过滤器

```java

public class RemoveCachedBodyFilter implements GlobalFilter, Ordered {

	private static final Log log = LogFactory.getLog(RemoveCachedBodyFilter.class);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return chain.filter(exchange).doFinally(s -> {
      //将网关上下文中的请求body删除
			Object attribute = exchange.getAttributes().remove(CACHED_REQUEST_BODY_ATTR);
			if (attribute != null && attribute instanceof PooledDataBuffer) {
				PooledDataBuffer dataBuffer = (PooledDataBuffer) attribute;
				if (dataBuffer.isAllocated()) {
					if (log.isTraceEnabled()) {
						log.trace("releasing cached body in exchange attribute");
					}
          //释放内存
					dataBuffer.release();
				}
			}
		});
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

}
```

> 此过滤器最简单，只做了一件事，就是删除网关上下文中的缓存；

GitHub地址：[https://github.com/mingyang66/EmilyGateway](https://github.com/mingyang66/EmilyGateway)