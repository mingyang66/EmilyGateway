### Gateway网关异常处理

#### 一、webflux框架全局异常处理

> 通过实现ErrorWebExceptionHandler接口，重写handle方法，替换掉框架默认的异常处理实现类DefaultErrorWebExceptionHandler

```java
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    /**
     * 处理给定的异常
     * @param exchange
     * @param ex
     * @return
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        BaseResponse baseResponse = new BaseResponse();
        if (ex instanceof ResponseStatusException) {
            baseResponse.setStatus(((ResponseStatusException) ex).getStatus().value());
            baseResponse.setMessage(((ResponseStatusException) ex).getReason());
        } else {
            baseResponse.setStatus(500);
            baseResponse.setMessage(ex.getMessage());
        }
        DataBuffer dataBuffer = response.bufferFactory()
                .allocateBuffer().write(JSONUtils.toJSONString(baseResponse).getBytes());
        response.setStatusCode(HttpStatus.OK);
        //基于流形式
        response.getHeaders().setContentType(MediaType.APPLICATION_NDJSON);
        return response.writeAndFlushWith(Mono.just(ByteBufMono.just(dataBuffer)));
    }
}
```

#### 二、circuitBreaker断路器异常处理

> SpringCloudCircuitBreakerFilterFactory拦截器工厂类apply方法中addExceptionDetails方法是处理断路器熔断时异常，handleErrorWithoutFallback方法是在未配置断路器跳转地址的时候异常处理；

```java
	private void addExceptionDetails(Throwable t, ServerWebExchange exchange) {
		ofNullable(t).ifPresent(
				exception -> exchange.getAttributes().put(CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR, exception));
	}
```

断路器异常会存在ServerWebExchange的属性CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR中，所以我们可以在断路器跳转地址中从此属性中获取异常信息；

```java
    @GetMapping("fallback")
    public String fallback(ServerWebExchange exchange){
        Throwable throwable = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        ServerWebExchange delegate = ((ServerWebExchangeDecorator) exchange).getDelegate();
        logger.error("服务调用失败，URL={}", delegate.getRequest().getURI(), throwable);
        return "Service Unavailable";
    }
```

handleErrorWithoutFallback方法的具体实现在SpringCloudCircuitBreakerResilience4JFilterFactory类中，其会在指定了断路器但是未指定跳转方法的时候处理异常，具体会被全局异常处理，如下：

```java
	@Override
	protected Mono<Void> handleErrorWithoutFallback(Throwable t, boolean resumeWithoutError) {
		if (java.util.concurrent.TimeoutException.class.isInstance(t)) {
			return Mono.error(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, t.getMessage(), t));
		}
		if (CallNotPermittedException.class.isInstance(t)) {
			return Mono.error(new ServiceUnavailableException());
		}
		if (resumeWithoutError) {
			return Mono.empty();
		}
		return Mono.error(t);
	}
```

