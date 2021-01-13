##### NettyWebServer开启http端口并实现http自动跳转https

##### 1.定义配置文件类

```java
package com.emily.cloud.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.server.Shutdown;

/**
 * @program: EmilyGateway
 * @description: Netty服务器配置
 * @create: 2021/01/13
 */
@ConfigurationProperties(prefix = "server.http")
public class ServerProperties {
    /**
     * 是否开启http端口号
     */
    private boolean enable;
    /**
     * http端口号
     */
    private int port = 8080;
    /**
     * 关闭模式
     */
    private Shutdown shutdown = Shutdown.IMMEDIATE;
    /**
     * http请求是否自动跳转到https
     */
    private boolean httpToHttps = true;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Shutdown getShutdown() {
        return shutdown;
    }

    public void setShutdown(Shutdown shutdown) {
        this.shutdown = shutdown;
    }

    public boolean isHttpToHttps() {
        return httpToHttps;
    }

    public void setHttpToHttps(boolean httpToHttps) {
        this.httpToHttps = httpToHttps;
    }
}

```

##### 2.配置NettyWebServer服务配置类，实现独立端口及自动跳转https

```java
package com.emily.cloud.gateway.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.HttpHandler;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @program: EmilyGateway
 * @description: 开启http端口
 * @create: 2021/01/13
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ServerProperties.class)
@ConditionalOnProperty(prefix = "server.http", name = "enable", havingValue = "true", matchIfMissing = true)
public class NettyWebServerAutoConfiguration {
    @Autowired
    private HttpHandler httpHandler;
    @Autowired
    private ServerProperties serverProperties;

    private WebServer webServer;

    @PostConstruct
    public void start() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory(serverProperties.getPort());
        //设置关机模式
        factory.setShutdown(serverProperties.getShutdown());
        if (serverProperties.isHttpToHttps()) {
            webServer = factory.getWebServer(((request, response) -> {
                URI uri = request.getURI();
                try {
                    response.getHeaders().setLocation(new URI(StringUtils.replace(uri.toString(), "http", "https")));
                } catch (URISyntaxException e) {
                    return Mono.error(e);
                }
                return response.setComplete();
            }));
        } else {
            factory.getWebServer(httpHandler);
        }
        webServer.start();
    }

    @PreDestroy
    public void stop() {
        webServer.stop();
    }

}

```

将上述两个加入系统之中，然后启动就可以实现开启独立http端口及http自动跳转https的能力；

GitHub地址：[https://github.com/mingyang66/EmilyGateway](https://github.com/mingyang66/EmilyGateway)

