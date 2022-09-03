### SpringCloud Gateway如何设置keep-alive

##### 一、Http中的keep-alive保活设置

- 短连接：请求-响应模式中发起请求时建立连接，响应后直接断开连接；
- 长连接：第一次发起请求后连接不断开，接下来的请求可以复用这个连接；
- HTTP1.0：当前协议版本中keep-alive默认是关闭的，需要在请求头中添加"Connection:Keep-Alive"，才能启用Keep-Alive；
- Http1.1：当前协议浏览器发起的默认都是Keep-Alive连接请求，默认的时间可以有客户端设置，也可以由服务端设置；

##### 二、SpringCloud Gateway保活机制超时时间

> 最近上了网关，有一个高并发的接口，导致通过监控发现客户端长期持有大量的连接不释放，严重影响到了网关的TPS，跟前端的同事了解Android、IOS端的Keep-Alive时间是30S，所以就想通过服务端来控制保活时长；

```properties
# 网络通道(channel)连接超时时间
server.netty.connection-timeout=PT10S
# 连接等待时间（毫秒），超时会被自动关闭。为空表示永远不关闭，全靠请求方
server.netty.idle-timeout=PT10S
```

通过idle-timeout属性配置更改超时时间为10S，通过监控观察连接数直接降低为了原来的三分之一。



GitHub地址：[https://github.com/mingyang66/EmilyGateway](https://github.com/mingyang66/EmilyGateway)