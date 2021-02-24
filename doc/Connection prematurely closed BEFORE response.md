 Connection prematurely closed BEFORE response
reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response异常解决

##### 一、最近在开发网关系统，就在感觉万事大吉可以上线的时候发现了如下的错误(这个是我在配置rabbitmq访问多个服务时发现的)

```java
 Connection prematurely closed BEFORE response
reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response
reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Error has been observed at the following site(s):
	|_ checkpoint ⇢ org.springframework.cloud.gateway.filter.WeightCalculatorWebFilter [DefaultWebFilterChain]
	|_ checkpoint ⇢ org.springframework.boot.actuate.metrics.web.reactive.server.MetricsWebFilter [DefaultWebFilterChain]
	|_ checkpoint ⇢ HTTP GET "/rabbitmq/api/vhosts" [ExceptionHandlingWebHandler]
```

什么意思呢？就是在请求还未响应的时候连接直接断开了，什么情况，崩溃，但是问题还是要解决于是开始了度娘之路，来看下如下这张图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210224131207157.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lhb21pbmd5YW5n,size_16,color_FFFFFF,t_70)

由上图可知发生异常的原因是从连接池中拿到连接之后发送请求，请求还未到达目标服务器就已经被目标服务器关闭了；而SCG中的连接还未被回收掉；

##### 二、解决方案

第一步添加JVM参数，更改从连接池中取连接的策略，由FIFO变更为LIFO（reactor.netty.resources.ConnectionProvider），确保拿到的连接永远是最新的连接；

```properties
-Dreactor.netty.pool.leasingStrategy=lifo
```

第二步设置连接空闲多久后会被回收掉，这个时间要比对应服务的回收时间小（tomcat对应的是server.tomcat.connection-timeout属性配置，在浏览器中看到的就是keep-Alive）,这样就可以确保SCG回收连接在后端服务之前进行，完美避开这个问题；

```properties
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          maxIdleTime: PT1S
```

GitHub地址：[https://github.com/mingyang66/EmilyGateway/tree/main/doc](https://github.com/mingyang66/EmilyGateway/tree/main/doc)

