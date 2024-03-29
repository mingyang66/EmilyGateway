package com.emily.infrastructure.gateway.config.server;

import com.emily.infrastructure.common.StringUtils;
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
 * @author Emily
 * @program: EmilyGateway
 * @description: 开启http端口
 * @create: 2021/01/13
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NettyWebServerProperties.class)
@ConditionalOnProperty(prefix = NettyWebServerProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = false)
public class NettyWebServerAutoConfiguration {

    private HttpHandler httpHandler;
    private NettyWebServerProperties nettyWebServerProperties;
    private WebServer webServer;

    public NettyWebServerAutoConfiguration(HttpHandler httpHandler, NettyWebServerProperties nettyWebServerProperties) {
        this.httpHandler = httpHandler;
        this.nettyWebServerProperties = nettyWebServerProperties;
    }

    @PostConstruct
    public void start() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory(nettyWebServerProperties.getPort());
        //设置关机模式
        factory.setShutdown(nettyWebServerProperties.getShutdown());
        if (nettyWebServerProperties.isHttpToHttps()) {
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
