#### 一、CircuitBreaker过滤器工厂

开启Spring Cloud CircuitBreaker过滤器，需在maven依赖配置中引入如下配置：

```pom
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
            <version>2.1.1</version>
        </dependency>
```

springcloud gateway中配置断路器，示例如下：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: circuitbreaker_route
        uri: lb://backing-service:8088
        predicates:
        - Path=/consumingServiceEndpoint
        filters:
        - name: CircuitBreaker
          args:
            name: myCircuitBreaker
            fallbackUri: forward:/inCaseOfFailureUseThis
```

> 使用fallbackUri在网关应用程序中定义内部控制器，