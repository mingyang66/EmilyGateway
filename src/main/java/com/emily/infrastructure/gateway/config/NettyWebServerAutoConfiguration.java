package com.emily.infrastructure.gateway.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

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
                //https://tools.ietf.org/html/rfc7231#section-6.4.2
                response.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
                return response.setComplete();
            }));
        } else {
            webServer = factory.getWebServer(httpHandler);
        }
        webServer.start();
    }

    @PreDestroy
    public void stop() {
        if (Objects.nonNull(webServer)) {
            webServer.stop();
        }
    }

}
