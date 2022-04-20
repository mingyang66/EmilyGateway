### 一、Path路由断言工厂

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

#### 二、Method路由断言工厂

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

#### 三、Weight权重路由断言工厂

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

#### 四、After路由断言工厂类

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

#### 四、Before路由断言工厂类

> Before路由断言工厂类只有一个ZonedDateTime类型参数datetime，所有的请求只会发生在datetime之前；

```
spring:
  cloud:
    gateway:
      routes:
      - id: before_route
        uri: https://example.org
        predicates:
        - Before=2017-01-20T17:42:47.789-07:00[America/Denver]
```

#### 五、Between路由断言工厂类

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

#### 六、RemoteAddr路由断言工厂类

> RemoteAddr路由断言工厂类有一个列表类型参数sources，可以指定路由请求的IP地址；其中192.168.1.1是IP地址，24是子网掩码；

```
spring:
  cloud:
    gateway:
      routes:
      - id: remoteaddr_route
        uri: https://example.org
        predicates:
        - RemoteAddr=192.168.1.1/24
```

