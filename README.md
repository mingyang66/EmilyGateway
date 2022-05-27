

## Emily网关服务

#### 一、项目依赖版本

- JDK11
- springboot2.6.7
- 基础库组件4.0.10
- springcloud3.1.0
- springcloud gateway3.1.2
- Resilience4j2.1.2

#### 二、支持功能

- 支持同时启动两个端口，一个http端口一个https端口，支持由http跳转到https；
- 支持全局捕获网关异常，打印日志到指定的日志文件，方便查问题；
- 支持基于Resilience4j的断路器熔断支持；
- 支持跨域全局设置；
- 支持对网关http|https对下游请求相关参数设置，如：连接超时时间等；

#### 三、网关配置示例

请参考项目目录下的application系列配置文件

#### 四、断言工厂配置示例

##### 1.Path路由断言工厂

> 路由断言工厂类有两个参数，patterns（基于spring的PathMatcher）、matchTrailingSlash（是否匹配斜杠，默认：true）

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: path_route
        uri: https://example.org
        predicates:
        - Path=/red/{segment},/blue/{segment}
```

- 支持'/foo/{&ast;foobar}' 和 '/{&ast;foobar}'格式，由CaptureTheRestPathElement提供支持；
- 支持'/foo/{bar}/goo'格式，将一段变量作为路径元素，由CaptureVariablePathElement提供支持；
- 支持'/foo/bar/goo'格式，由LiteralPathElement提供支持；
- 支持'/foo/&ast;/goo'通配符格式，&ast;代表至少匹配一个字符，由WildcardPathElement提供支持；
- 支持'/foo/**' 和 /&ast;&ast;  Rest通配符格式，匹配0个或者多个目录，由WildcardTheRestPathElement提供支持；
- 支持'/foo/??go'单字符通配符格式，一个？代表单个字符，若需要适配多个可用多个？标识，由SingleCharWildcardedPathElement提供支持；
- 支持/foo/&ast;_&ast;/&ast;\_{foobar}格式，由RegexPathElement提供支持；

##### 2.Method路由断言工厂

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: method_route
        uri: https://example.org
        predicates:
        - Method=GET,POST
```

> Method路由断言工厂有一个参数Metod，指定多个支持的请求方法；支持的方法：GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE

##### 3.Weight权重路由断言工厂

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: weight_high
        uri: https://weighthigh.org
        predicates:
        - Weight=group1, 8
      - id: weight_low
        uri: https://weightlow.org
        predicates:
        - Weight=group1, 2
```

> Weight权重路由断言工厂有两个参数group（分组名称）和weight（权重 int类型），每组按照权重计算流量

##### 4.After路由断言工厂类

> After路由断言工厂类带有一个参数datetime（是ZonedDateTime类型），此断言工厂类判定所有请求只有发生在指定的时间之后才符合条件；

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: after_route
        uri: https://example.org
        predicates:
        - After=2022-04-20T15:35:08.721398+08:00[Asia/Shanghai]
```

获取带区域时间方法如下：

```java
    public static void main(String[] args) {
        ZonedDateTime zbj = ZonedDateTime.now();
        ZonedDateTime zny = ZonedDateTime.now(ZoneId.of("America/New_York"));
        System.out.println(zbj);
        System.out.println(zny);
    }
```

##### 5.Before路由断言工厂类

> Before路由断言工厂类只有一个ZonedDateTime类型参数datetime，所有的请求只会发生在datetime之前；

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: before_route
        uri: https://example.org
        predicates:
        - Before=2017-01-20T17:42:47.789-07:00[America/Denver]
```

##### 6.Between路由断言工厂类

> Between路由断言工厂类有两个ZonedDateTime类型参数datetime1、datetime2,请求必须发生在datetime1和datetime2之间，datetime2必须小于datetime1;

```
spring:
  cloud:
    gateway:
      routes:
      - id: between_route
        uri: https://example.org
        predicates:
        - Between=2017-01-20T17:42:47.789-07:00[America/Denver], 2017-01-21T17:42:47.789-07:00[America/Denver]
```

##### 6.RemoteAddr路由断言工厂类

> RemoteAddr路由断言工厂类有一个列表类型参数sources，可以指定路由请求的IP地址；其中192.168.1.1是IP地址，24是子网掩码；

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: remoteaddr_route
        uri: https://example.org
        predicates:
        - RemoteAddr=192.168.1.1/24
```

#### 五、过滤器工厂配置示例

##### 1.DedupeResponseHeader消除重复响应头过滤器工厂类

> DedupeResponseHeader过滤器工厂类有两个可选参数name和strategy，name可以指定用逗号分隔的header名称列表。strategy指定保留重复header的策略，有三个选择RETAIN_FIRST-保留第一个值（默认）、RETAIN_LAST-保留最后一个值、RETAIN_UNIQUE-保留唯一值

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: dedupe_response_header_route
        uri: https://example.org
        filters:
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
```

##### 2.PreserveHostHeader传递原始Host过滤器工厂

> PreserveHostHeader 过滤器工厂没有参数，此过滤器设置一个属性来决定是否发送原始Host，而不是通过Http客户端来决定Host。

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: preserve_host_route
        uri: https://example.org
        filters:
        - PreserveHostHeader
```

#### 六、故障诊断

##### 1.以下包可通过设置日志级别进行重要故障诊断信息打印

- `org.springframework.cloud.gateway`
- `org.springframework.http.server.reactive`
- `org.springframework.web.reactive`
- `org.springframework.boot.autoconfigure.web`
- `reactor.netty`
- `redisratelimiter`

以上包可以通过如下配置开启日志调试模式：

```yaml
logging:
  level:
    reactor.netty: debug
```

Reactor Netty HttpClient和HttpServer可以开启监控，当包reactor.netty日志级别设置为DEBUG或TRACE结合起来后，它可以打印相关日志信息，如：请求header和body，接收到的响应信息，以下是开启监控模式配置：

```
spring:
  cloud:
    gateway:
      httpclient:
        # 为Netty HttpClient启用监听调试，默认：false
        wiretap: true
      httpserver:
        # 为Netty HttpServer开启监听debugging模式，默认：false
        wiretap: true
```

